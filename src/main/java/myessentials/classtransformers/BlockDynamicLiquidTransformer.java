package myessentials.classtransformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

public class BlockDynamicLiquidTransformer implements IClassTransformer {

    private class UpdateTickGeneratorAdapter extends GeneratorAdapter {
        private int patch =0;
        private int waitingIF_ACMPNE;
        private Label cancelledLabel;
        private boolean waitingALoad1;
        private int toX, toY, toZ, num;

        protected UpdateTickGeneratorAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM4, mv, access, name, desc);
            toX = newLocal(Type.INT_TYPE);
            toY = newLocal(Type.INT_TYPE);
            toZ = newLocal(Type.INT_TYPE);
            num = newLocal(Type.INT_TYPE);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if(patch == 0 && name.equals("func_149809_q"))
                waitingIF_ACMPNE = 2;
            else if(patch == 1 && opcode == Opcodes.INVOKESPECIAL && name.equals("func_149813_h")) {
                // previous operations
                // Stack: Block, World, toX, toY, toZ, num

                super.storeLocal(num);
                super.storeLocal(toZ);
                super.storeLocal(toY);
                // Stack: Block, World, toX

                super.visitInsn(Opcodes.DUP);
                super.storeLocal(toX);
                // Stack: Block, World, toX

                super.loadLocal(toY);
                super.loadLocal(toZ);
                super.loadLocal(num);
                // Stack: Block, World, toX, toY, toZ, num

                super.visitVarInsn(Opcodes.ILOAD, 2);
                super.visitVarInsn(Opcodes.ILOAD, 3);
                super.visitVarInsn(Opcodes.ILOAD, 4);
                // Stack: Block, World, toX, toY, toZ, num, x, y, z

                super.visitMethodInsn(Opcodes.INVOKESTATIC, "myessentials/event/LiquidFlowEvent",
                        "fireEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIIIIII)Z", false
                );
                // Stack: result

                Label elseLabel = new Label();
                super.visitJumpInsn(Opcodes.IFNE, elseLabel);
                // Stack:

                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.loadLocal(toX);
                super.loadLocal(toY);
                super.loadLocal(toZ);
                super.loadLocal(num);
                // Stack: Block, World, toX, toY, toZ, num

                super.visitMethodInsn(opcode, owner, name, desc, itf);
                // Stack:

                super.visitLabel(elseLabel);
                //super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                return;
            }

            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if(patch == 0 && waitingIF_ACMPNE > 0 && opcode == Opcodes.IF_ACMPNE) {
                cancelledLabel = label;
                waitingIF_ACMPNE--;
                if(waitingIF_ACMPNE == 0)
                    waitingALoad1 = true;
            }

            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if(patch == 0 && waitingALoad1 && opcode == Opcodes.ALOAD && var == 1) {
                waitingALoad1 = false;
                patch = 1;

                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ILOAD, 2);
                super.visitVarInsn(Opcodes.ILOAD, 3);
                super.visitVarInsn(Opcodes.ILOAD, 4);
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "myessentials/event/LiquidReplaceBlockEvent", "fireEvent",
                        "(Lnet/minecraft/block/Block;IIILnet/minecraft/world/World;)Z", false);

                super.visitJumpInsn(Opcodes.IFNE, cancelledLabel);
            }

            super.visitVarInsn(opcode, var);
        }
    }

    @Override
    public byte[] transform(String name, String srgName, byte[] bytes) {
        if("net.minecraft.block.BlockDynamicLiquid".equals(srgName)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4 | ClassWriter.COMPUTE_FRAMES);

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                    if("func_149674_a".equals(name) || "updateTick".equals(name))
                        return new UpdateTickGeneratorAdapter(methodVisitor, access, name, desc);

                    return methodVisitor;
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            bytes = writer.toByteArray();
        }

        return bytes;
    }
}
