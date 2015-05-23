package net.sothatsit.gamepackdownloader.descriptor;

public class FieldDescriptor extends Descriptor {

    private String type;

    public FieldDescriptor(String descriptorRaw, ClassNameSupplier supplier) {
        super(descriptorRaw);

        if(descriptorRaw == null) {
            return;
        }

        this.type = Descriptor.getFullName(descriptorRaw, supplier);
    }

    @Override
    public String getDescriptorReformatted() {
        return (getDescriptorRaw() == null ? null : Descriptor.getShortName(type));
    }

    @Override
    public String getWorkingDescriptor() {
        return (getDescriptorRaw() == null ? null : Descriptor.getDescriptorName(type));
    }

    public String getType() {
        return type;
    }

}
