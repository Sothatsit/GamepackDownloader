package net.sothatsit.gamepackdownloader.refactor;

import net.sothatsit.gamepackdownloader.GamePackDownloader;
import net.sothatsit.gamepackdownloader.io.JarArchive;
import net.sothatsit.gamepackdownloader.io.JarResourceRenamer;
import net.sothatsit.gamepackdownloader.refactor.descriptor.Descriptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefactorMapBuilder {

    private IClassRenamer classRenamer;
    private IFieldRenamer fieldRenamer;
    private IMethodRenamer methodRenamer;

    protected RefactorMapBuilder(IClassRenamer classRenamer, IFieldRenamer fieldRenamer, IMethodRenamer methodRenamer) {
        this.classRenamer = classRenamer;
        this.fieldRenamer = fieldRenamer;
        this.methodRenamer = methodRenamer;
    }

    public RefactorMap build(JarArchive archive) throws IOException {
        ClassMap classMap = ClassMap.build(archive);

        RefactorMap map = new RefactorMap();

        if(classRenamer instanceof JarResourceRenamer) {
            JarResourceRenamer renamer = (JarResourceRenamer) classRenamer;

            renamer.setClassMap(classMap);

            do {
                renamer.softReset();
                for(ClassMap.MapClass clazz : classMap.getClasses()) {
                    String refactored = classRenamer.getNewName(clazz.getVersion(), clazz.getAccess(), clazz.getName(), clazz.getSignature(), clazz.getSuperName(), clazz.getInterfaces());

                    if(refactored.equals(clazz.getName())) {
                        continue;
                    }

                    GamePackDownloader.info("Refactored class \"" + clazz.getName() + "\" to \"" + refactored + "\"");

                    map.setClassName(clazz.getName(), refactored);
                }
            } while (renamer.hasHitRoadBlock());
        } else {
            for(ClassMap.MapClass clazz : classMap.getClasses()) {
                String refactored = classRenamer.getNewName(clazz.getVersion(), clazz.getAccess(), clazz.getName(), clazz.getSignature(), clazz.getSuperName(), clazz.getInterfaces());

                if(refactored.equals(clazz.getName())) {
                    continue;
                }

                GamePackDownloader.info("Refactored class \"" + clazz.getName() + "\" to \"" + refactored + "\"");

                map.setClassName(clazz.getName(), refactored);
            }
        }

        for(ClassMap.MapClass clazz : classMap.getClasses()) {
            String className = map.getNewClassName(clazz.getName());

            List<ClassMap.MapClass> implementingClasses = clazz.getImplementingClasses();
            List<ClassMap.MapClass> superMapClasses = new ArrayList<>();
            List<Class<?>> superClasses = new ArrayList<>();

            List<String> check = new ArrayList<>(Arrays.asList(clazz.getInterfaces()));
            if(clazz.getSuperName() != null) {
                check.add(clazz.getSuperName());
            }

            for(String clazzName : clazz.getInterfaces()) {
                ClassMap.MapClass c = classMap.getMapClass(clazzName);

                if(c != null) {
                    superMapClasses.add(c);
                } else {
                    Class<?> c2 = Descriptor.getClass(clazzName);

                    if(c2 != null) {
                        superClasses.add(c2);
                    }
                }
            }

            for(ClassMap.MapField field : clazz.getFields()) {
                if(field.getName().length() > 2) {
                    continue;
                }

                String refactored = fieldRenamer.getNewName(className, field.getAccess(), field.getName(), field.getDesc(), field.getSignature(), field.getValue());

                if(refactored.equals(field.getName())) {
                    continue;
                }

                GamePackDownloader.info("Refactored field \"" + field.getName() + "\" of class \"" + className + "\" to \"" + refactored + "\"");

                map.setFieldName(clazz.getName(), field.getName(), refactored);
            }

            methods: for(ClassMap.MapMethod method : clazz.getMethods()) {
                if(method.getName().length() > 2) {
                    continue;
                }

                for(ClassMap.MapClass mapClass : superMapClasses) {
                    ClassMap.MapMethod m = mapClass.getMethod(method.getName());

                    if(m != null && m.getDesc().equals(method.getDesc())) {
                        continue methods;
                    }
                }

                Class<?> returnType = Descriptor.getClass(method.getReturnType());
                Class<?>[] arguments = new Class<?>[method.getArguments().length];

                boolean checkClasses = true;
                for(int i=0; i < arguments.length; i++) {
                    arguments[i] = Descriptor.getClass(method.getArguments()[i]);

                    if(arguments[i] == null) {
                        checkClasses = false;
                        break;
                    }
                }

                if(checkClasses) {
                    for(Class<?> c : superClasses) {
                        for(Method m : c.getMethods()) {
                            if(!m.getName().equals(method.getName()) || (returnType != null && !returnType.equals(m.getReturnType()))) {
                                continue;
                            }

                            if(m.getParameterCount() != arguments.length) {
                                continue;
                            }

                            for(int i=0; i < arguments.length; i++) {
                                if(!arguments[i].equals(m.getParameterTypes()[i])) {
                                    continue;
                                }
                            }

                            continue methods;
                        }
                    }
                }

                String refactored = methodRenamer.getNewName(className, method.getAccess(), method.getName(), method.getDesc(), method.getSignature(), method.getExceptions());

                if(refactored.equals(method.getName())) {
                    continue;
                }

                GamePackDownloader.info("Refactored method \"" + method.getName() + "\" of class \"" + className + "\" to \"" + refactored + "\"");

                map.setMethodName(className, method.getName(), refactored);
            }
        }

        for(ClassMap.MapClass clazz : classMap.getClasses()) {
            for(ClassMap.MapMethod method : clazz.getMethods()) {
                String newName = map.getNewMethodName(clazz.getName(), method.getName());

                if(newName.equals(method.getName())) {
                    continue;
                }

                for(ClassMap.MapClass c : clazz.getImplementingClasses()) {
                    if(c == null) {
                        continue;
                    }

                    ClassMap.MapMethod m = c.getMethod(method.getName());

                    if(m != null && m.getDesc().equals(method.getDesc())) {
                        map.setMethodName(c.getName(), m.getName(), newName);
                    }
                }
            }
        }

        map.fixDuplicates();

        return map;
    }

}
