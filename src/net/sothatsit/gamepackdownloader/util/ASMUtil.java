package net.sothatsit.gamepackdownloader.util;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ASMUtil {

    public static boolean isStatic(MethodNode method) {
        return (method.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isStatic(FieldNode field) {
        return (field.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isAbstract(ClassNode clazz) {
        return (clazz.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isAbstract(MethodNode method) {
        return (method.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isInterface(ClassNode clazz) {
        return (clazz.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public static boolean areSimilar(InsnList list1, InsnList list2) {
        if(list1.size() != list2.size()) {
            return false;
        }

        for(int i=0; i<list1.size(); i++) {
            AbstractInsnNode n1 = list1.get(i);
            AbstractInsnNode n2 = list2.get(i);

            if(!areSimilar(n1, n2)) {
                return false;
            }
        }

        return true;
    }

    public static boolean areSimilar(AbstractInsnNode n1, AbstractInsnNode n2) {
        if(n1.getOpcode() != n2.getOpcode()) {
            return false;
        }

        // Local field in method
        if(n1 instanceof FieldInsnNode && n2 instanceof FieldInsnNode) {
            FieldInsnNode f1 = (FieldInsnNode) n1;
            FieldInsnNode f2 = (FieldInsnNode) n2;

            if(!f1.desc.equals(f2.desc)) {
                return false;
            }
        }

        // Not necessary, does not affect code flow
        //if(n1 instanceof FrameNode && n2 instanceof FrameNode) {
        //    FrameNode f1 = (FrameNode) n1;
        //    FrameNode f2 = (FrameNode) n2;
        //}

        // Increments local variable var by incr
        if(n1 instanceof IincInsnNode && n2 instanceof IincInsnNode) {
            IincInsnNode i1 = (IincInsnNode) n1;
            IincInsnNode i2 = (IincInsnNode) n2;

            if(i1.incr != i2.incr || i1.var != i2.var) {
                return false;
            }
        }

        // Represents zero operand instruction, contains no new information
        //if(n1 instanceof InsnNode && n2 instanceof InsnNode) {
        //    InsnNode i1 = (InsnNode) n1;
        //    InsnNode i2 = (InsnNode) n2;
        //}

        // An instruction with a single int operand
        if(n1 instanceof IntInsnNode && n2 instanceof IntInsnNode) {
            IntInsnNode i1 = (IntInsnNode) n1;
            IntInsnNode i2 = (IntInsnNode) n2;

            if(i1.operand != i2.operand) {
                return false;
            }
        }

        // Only added from non-java languages which run on the jvm
        //if(n1 instanceof InvokeDynamicInsnNode && n2 instanceof InvokeDynamicInsnNode) {
        //    InvokeDynamicInsnNode i1 = (InvokeDynamicInsnNode) n1;
        //    InvokeDynamicInsnNode i2 = (InvokeDynamicInsnNode) n2;
        //}


        // Jumps to another instruction
        if(n1 instanceof JumpInsnNode && n2 instanceof JumpInsnNode) {
            JumpInsnNode j1 = (JumpInsnNode) n1;
            JumpInsnNode j2 = (JumpInsnNode) n2;

            if(!areSimilar(j1.label, j2.label)) {
                return false;
            }
        }

        // Encapsulates Label as a node, Label designates the instruction directly after itself, used for jumps/go to's
        if(n1 instanceof LabelNode && n2 instanceof LabelNode) {
            if(!areSimilar((LabelNode) n1, (LabelNode) n2)) {
                return false;
            }
        }

        // Push a constant from constant pool to stack
        if(n1 instanceof LdcInsnNode && n2 instanceof LdcInsnNode) {
            LdcInsnNode l1 = (LdcInsnNode) n1;
            LdcInsnNode l2 = (LdcInsnNode) n2;

            if((l1.cst == null || l2.cst == null ? l1.cst != null || l2.cst != null : !l1.cst.equals(l2.cst))) {
                return false;
            }
        }

        // Does not affect control flow
        //if(n1 instanceof LineNumberNode && n2 instanceof LineNumberNode) {
        //    LineNumberNode l1 = (LineNumberNode) n1;
        //    LineNumberNode l2 = (LineNumberNode) n2;
        //}

        // Lookup switch statement
        if(n1 instanceof LookupSwitchInsnNode && n2 instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode s1 = (LookupSwitchInsnNode) n1;
            LookupSwitchInsnNode s2 = (LookupSwitchInsnNode) n2;

            if(!areSimilar(s1.dflt, s2.dflt) || s1.labels.size() != s2.labels.size() || s1.keys.size() != s2.keys.size()) {
                return false;
            }

            List<LabelNode> checked = new ArrayList<>(s1.labels);
            for(int i=0; i<s1.labels.size(); i++) {
                LabelNode ln2 = s2.labels.get(i);
                int index = -1;

                for(int i2=0; i2<checked.size(); i2++) {
                    LabelNode ln1 = checked.get(i2);
                    if(areSimilar(ln1, ln2)) {
                        index = i2;
                        break;
                    }
                }

                if(index < 0) {
                    return false;
                }

                checked.remove(index);
            }

            List<Integer> check = new ArrayList<>(s2.keys);
            for(int i=0; i<s1.keys.size(); i++) {
                Integer int1 = s1.keys.get(i);
                int index = -1;

                for(int i2=0; i2<check.size(); i2++) {
                    Integer int2 = check.get(i2);
                    if(int2 == int1) {
                        index = i2;
                        break;
                    }
                }

                if(index < 0) {
                    return false;
                }

                check.remove(index);
            }
        }

        // invokes a method
        if(n1 instanceof MethodInsnNode && n2 instanceof MethodInsnNode) {
            MethodInsnNode m1 = (MethodInsnNode) n1;
            MethodInsnNode m2 = (MethodInsnNode) n2;

            if(!m1.owner.equals(m2.owner) || !m1.name.equals(m2.name) || !m1.desc.endsWith(m2.desc)) {
                return false;
            }
        }

        // Allocates a new array
        if(n1 instanceof MultiANewArrayInsnNode && n2 instanceof MultiANewArrayInsnNode) {
            MultiANewArrayInsnNode m1 = (MultiANewArrayInsnNode) n1;
            MultiANewArrayInsnNode m2 = (MultiANewArrayInsnNode) n2;

            if(!m1.desc.equals(m2.desc) || m1.dims != m2.dims) {
                return false;
            }
        }

        // Table switch statement
        if(n1 instanceof TableSwitchInsnNode && n2 instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode t1 = (TableSwitchInsnNode) n1;
            TableSwitchInsnNode t2 = (TableSwitchInsnNode) n2;

            if(!areSimilar(t1.dflt, t2.dflt) || t1.labels.size() != t2.labels.size() || t1.min != t2.min || t1.max != t2.max) {
                return false;
            }

            List<LabelNode> checked = new ArrayList<>(t1.labels);
            for(int i=0; i<t1.labels.size(); i++) {
                LabelNode ln2 = t2.labels.get(i);
                int index = -1;

                for(int i2=0; i2<checked.size(); i2++) {
                    LabelNode ln1 = checked.get(i2);
                    if(areSimilar(ln1, ln2)) {
                        index = i2;
                        break;
                    }
                }

                if(index < 0) {
                    return false;
                }

                checked.remove(index);
            }
        }


        if(n1 instanceof TypeInsnNode && n2 instanceof TypeInsnNode) {
            TypeInsnNode t1 = (TypeInsnNode) n1;
            TypeInsnNode t2 = (TypeInsnNode) n2;

            if(!t1.desc.equals(t2.desc)) {
                return false;
            }
        }

        if(n1 instanceof VarInsnNode && n2 instanceof VarInsnNode) {
            VarInsnNode v1 = (VarInsnNode) n1;
            VarInsnNode v2 = (VarInsnNode) n2;

            if(v1.var != v2.var) {
                return false;
            }
        }

        return true;
    }

    public static boolean areSimilar(LabelNode ln1, LabelNode ln2) {
        Label l1 = ln1.getLabel();
        Label l2 = ln2.getLabel();

        return l1.getOffset() == l2.getOffset() && (l1.info == null || l2.info == null ? l1.info == null && l2.info == null : l1.info.equals(l2.info));
    }

}
