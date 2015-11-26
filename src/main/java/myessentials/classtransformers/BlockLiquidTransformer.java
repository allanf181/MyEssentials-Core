package myessentials.classtransformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

public class BlockLiquidTransformer implements IClassTransformer {
    private class BlockLiquidGeneratorAdapter extends GeneratorAdapter {
        private int fromX, fromY, fromZ;
        private int waitingIF_ACMPNE;
        private boolean patched;

        protected BlockLiquidGeneratorAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM4, mv, access, name, desc);
            fromX = newLocal(Type.INT_TYPE);
            fromY = newLocal(Type.INT_TYPE);
            fromZ = newLocal(Type.INT_TYPE);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if(!patched && waitingIF_ACMPNE == 0 && opcode == Opcodes.GETSTATIC && owner.equals("net/minecraft/block/material/Material"))
            {
                waitingIF_ACMPNE = 6;
            }

            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if(!patched && waitingIF_ACMPNE > 0 && opcode == Opcodes.INVOKEVIRTUAL && owner.equals("net/minecraft/world/World") && desc.equals("(III)Lnet/minecraft/block/Block;")) {
                super.storeLocal(fromZ);
                super.storeLocal(fromY);
                super.visitInsn(Opcodes.DUP);
                super.storeLocal(fromX);
                super.loadLocal(fromY);
                super.loadLocal(fromZ);
            }

            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if(!patched && waitingIF_ACMPNE > 0 && opcode == Opcodes.IF_ACMPNE) {
                waitingIF_ACMPNE--;
                if(waitingIF_ACMPNE < 5) {
                    super.visitJumpInsn(opcode, label);

                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitVarInsn(Opcodes.ALOAD, 1);
                    super.loadLocal(fromX);
                    super.loadLocal(fromY);
                    super.loadLocal(fromZ);
                    super.visitVarInsn(Opcodes.ILOAD, 2);
                    super.visitVarInsn(Opcodes.ILOAD, 3);
                    super.visitVarInsn(Opcodes.ILOAD, 4);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "myessentials/event/LiquidReplaceBlockEvent", "fireEvent",
                            "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIIIII)Z", false);

                    super.visitJumpInsn(Opcodes.IFNE, label);
                    if(waitingIF_ACMPNE == 0)
                        patched = true;
                    return;
                }
            }

            super.visitJumpInsn(opcode, label);
        }
    }

    @Override
    public byte[] transform(String name, String srgName, byte[] bytes) {
        if("net.minecraft.block.BlockLiquid".equals(srgName)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4 | ClassWriter.COMPUTE_FRAMES);

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

                    if("func_149805_n".equals(name))
                        return new BlockLiquidGeneratorAdapter(methodVisitor, access, name, desc);

                    return methodVisitor;
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            bytes = writer.toByteArray();
        }

        return bytes;
    }
}
