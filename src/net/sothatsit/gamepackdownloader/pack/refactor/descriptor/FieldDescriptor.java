package net.sothatsit.gamepackdownloader.pack.refactor.descriptor;

import net.sothatsit.gamepackdownloader.refactor.ClassNameStore;

public class FieldDescriptor extends Descriptor {

    private String type;

    public FieldDescriptor(String descriptorRaw, ClassNameStore store) {
        super(descriptorRaw);

        this.type = Descriptor.getFullName(descriptorRaw, store);
    }

    @Override
    public String getDescriptorReformatted() {
        return type;
    }

    public String getType() {
        return type;
    }

}
