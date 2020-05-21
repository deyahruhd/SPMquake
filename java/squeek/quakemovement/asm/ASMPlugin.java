package squeek.quakemovement.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ASMPlugin implements IFMLLoadingPlugin, IClassTransformer
{
	public static boolean isObfuscated = false;
	public static boolean isClient = false;
	private static String CLASS_ENTITY_PLAYER      		= "net.minecraft.entity.player.EntityPlayer";
	private static String CLASS_ENTITY 			   		= "net.minecraft.entity.Entity";
	private static String CLASS_ENTITY_RENDERER         = "net.minecraft.client.renderer.EntityRenderer";
	private static String CLASS_NET_HANDLER_PLAY_CLIENT = "net.minecraft.client.network.NetHandlerPlayClient";
	private static String CLASS_SPACKET_EXPLOSION 		= "net.minecraft.network.play.server.SPacketExplosion";

	private static String CLASS_QUAKE_CLIENT_PLAYER 	= "squeek.quakemovement.movement.QuakeClientPlayer";
	private static String CLASS_QUAKE_SERVER_PLAYER 	= "squeek.quakemovement.movement.QuakeServerPlayer";

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (transformedName.equals(CLASS_ENTITY_PLAYER)) {
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode method;

			method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "travel", "(FFF)V");
			if (method == null)
				throw new RuntimeException("could not find EntityPlayer.travel");

			InsnList loadParameters = new InsnList();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 2));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 3));
			injectStandardHook(method, findFirstInstruction(method), CLASS_QUAKE_CLIENT_PLAYER, "moveEntityWithHeading", toMethodDescriptor("Z", CLASS_ENTITY_PLAYER, "F", "F", "F"), loadParameters);

			method = findMethodNodeOfClass(classNode, isObfuscated ? "n" : "onLivingUpdate", "()V");
			if (method == null)
				throw new RuntimeException("could not find EntityPlayer.onLivingUpdate");

			loadParameters.clear();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			injectSimpleHook(method, findFirstInstruction(method), CLASS_QUAKE_CLIENT_PLAYER, "beforeOnLivingUpdate", toMethodDescriptor("V", CLASS_ENTITY_PLAYER), loadParameters);

			method = findMethodNodeOfClass(classNode, isObfuscated ? "cu" : "jump", "()V");
			if (method == null)
				throw new RuntimeException("could not find EntityPlayer.jump");

			loadParameters.clear();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			injectSimpleHook(method, findLastInstructionWithOpcode(method, Opcodes.RETURN), CLASS_QUAKE_CLIENT_PLAYER, "afterJump", toMethodDescriptor("V", CLASS_ENTITY_PLAYER), loadParameters);

			method = findMethodNodeOfClass(classNode, isObfuscated ? "e" : "fall", "(FF)V");
			if (method == null)
				throw new RuntimeException("could not find EntityPlayer.fall");

			loadParameters.clear();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			injectSimpleHook(method, findFirstInstruction(method), CLASS_QUAKE_SERVER_PLAYER, "beforeFall", toMethodDescriptor("V", CLASS_ENTITY_PLAYER, "F", "F"), loadParameters);

			loadParameters.clear();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			injectSimpleHook(method, findLastInstructionWithOpcode(method, Opcodes.RETURN), CLASS_QUAKE_SERVER_PLAYER, "afterFall", toMethodDescriptor("V", CLASS_ENTITY_PLAYER, "F", "F"), loadParameters);

			return writeClassToBytes(classNode);
		} else if (transformedName.equals(CLASS_ENTITY)) {
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode method;

			method = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "moveRelative", "(FFFF)V");
			if (method == null)
				throw new RuntimeException("could not find Entity.moveRelative");

			InsnList loadParameters = new InsnList();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 0));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 1));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 2));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 3));
			loadParameters.add(new VarInsnNode(Opcodes.FLOAD, 4));
			injectStandardHook(method, findFirstInstruction(method), CLASS_QUAKE_CLIENT_PLAYER, "moveRelativeBase", toMethodDescriptor("Z", CLASS_ENTITY, "F", "F", "F", "F"), loadParameters);

			return writeClassToBytes (classNode);
		} else if (transformedName.equals(CLASS_NET_HANDLER_PLAY_CLIENT)) {
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode method;

			String packetVelName = isObfuscated ? "kf" : "net/minecraft/network/play/server/SPacketEntityVelocity";
			String explosionName = isObfuscated ? "amp" : "net/minecraft/world/Explosion";
			String entityName    = isObfuscated ? "vg" : "net/minecraft/entity/Entity";
			String packetExpName = isObfuscated ? "ja" : "net/minecraft/network/play/server/SPacketExplosion";

			method = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "handleEntityVelocity", "(L" + packetVelName + ";)V");
			if (method == null)
				throw new RuntimeException("could not find NetHandlerPlayClient.handleExplosion");

			AbstractInsnNode setVel = findLastInstructionWithOpcode (method, Opcodes.INVOKEVIRTUAL);

			method.instructions.insertBefore(setVel, new MethodInsnNode(Opcodes.INVOKESTATIC, toInternalClassName(CLASS_QUAKE_CLIENT_PLAYER), "setEntityVelocity", "(L" + entityName + ";DDD)V", false));
			method.instructions.remove (setVel);

			MethodNode method2;

			method2 = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "handleExplosion", "(L" + packetExpName + ";)V");
			if (method2 == null)
				throw new RuntimeException("could not find NetHandlerPlayClient.handleExplosion");

			AbstractInsnNode doExplosionB =
					getOrFindInstructionWithOpcode (getOrFindInstructionWithOpcode (method2.instructions.getLast (), Opcodes.INVOKESPECIAL, true),
													Opcodes.INVOKEVIRTUAL, false);

			InsnList loadParameters = new InsnList();
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 2));
			loadParameters.add(new VarInsnNode(Opcodes.ALOAD, 1));
			loadParameters.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInternalClassName(CLASS_QUAKE_CLIENT_PLAYER), "applyExplosionToSPPlayer", "(L" + explosionName + ";L" + packetExpName + ";)V", false));
			loadParameters.add(new InsnNode(Opcodes.RETURN));

			method2.instructions.insertBefore (doExplosionB, loadParameters);
			return writeClassToBytes (classNode);
		} else if (transformedName.equals (CLASS_ENTITY_RENDERER)) {
			ClassNode classNode = readClassFromBytes(bytes);





			MethodNode applyBobbing = findMethodNodeOfClass(classNode, isObfuscated ? "f" : "applyBobbing", "(F)V");
			if (applyBobbing == null)
				throw new RuntimeException("could not find EntityRenderer.applyBobbing");

			InsnList loadParameters = new InsnList ();
			loadParameters.add (new VarInsnNode (Opcodes.FLOAD, 1));
			injectStandardHook (applyBobbing, applyBobbing.instructions.getFirst (), CLASS_QUAKE_CLIENT_PLAYER, "applyNormalBobbing", "(F)Z", loadParameters);





			MethodNode setupCameraTransform = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "setupCameraTransform", "(FI)V");
			if (setupCameraTransform == null)
				throw new RuntimeException("could not find EntityRenderer.setupCameraTransform");

			InsnList loadParameters1 = new InsnList ();
			loadParameters1.add (new VarInsnNode (Opcodes.FLOAD, 1));
			loadParameters1.add (new InsnNode (Opcodes.ICONST_0));

			AbstractInsnNode firstBobbingCall = getOrFindMethodInsn (setupCameraTransform.instructions.getFirst (), isObfuscated ? "f" : "applyBobbing", "(F)V", false);

			if (firstBobbingCall == null)
				throw new RuntimeException("bobbing call was not found in setupCameraTransform");

			injectStandardHook (setupCameraTransform, firstBobbingCall.getNext (), CLASS_QUAKE_CLIENT_PLAYER, "applyCustomBobbing", "(FZ)Z",
					loadParameters1);





			MethodNode renderHand = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "renderHand", "(FI)V");
			if (renderHand == null)
				throw new RuntimeException("could not find EntityRenderer.renderHand");

			InsnList loadParameters2 = new InsnList ();
			loadParameters2.add (new VarInsnNode (Opcodes.FLOAD, 1));
			loadParameters2.add (new InsnNode (Opcodes.ICONST_1));

			AbstractInsnNode secondBobbingCall = getOrFindMethodInsn (renderHand.instructions.getFirst (), isObfuscated ? "f" : "applyBobbing", "(F)V", false);

			if (secondBobbingCall == null)
				throw new RuntimeException("1st bobbing call was not found in renderHand");

			injectStandardHook (renderHand, secondBobbingCall.getNext (), CLASS_QUAKE_CLIENT_PLAYER, "applyCustomBobbing", "(FZ)Z",
					loadParameters2);

			AbstractInsnNode lastBobbingCall = getOrFindMethodInsn (renderHand.instructions.getLast (), isObfuscated ? "f" : "applyBobbing", "(F)V", true);

			if (lastBobbingCall == null)
				throw new RuntimeException("2nd bobbing call was not found in renderHand");

			InsnList loadParameters3 = new InsnList ();
			loadParameters3.add (new VarInsnNode (Opcodes.FLOAD, 1));
			loadParameters3.add (new InsnNode (Opcodes.ICONST_1));

			injectStandardHook (renderHand, lastBobbingCall.getNext (), CLASS_QUAKE_CLIENT_PLAYER, "applyCustomBobbing", "(FZ)Z",
					loadParameters3);

			return writeClassToBytes (classNode);
		}

		return bytes;
	}

	private void injectStandardHook(MethodNode method, AbstractInsnNode node, String hookClassName, String hookMethodName, String hookMethodDescriptor, InsnList loadParameters)
	{
		InsnList toInject = new InsnList();
		LabelNode ifNotCanceled = new LabelNode();
		toInject.add(loadParameters);
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInternalClassName(hookClassName), hookMethodName, hookMethodDescriptor, false));
		toInject.add(new JumpInsnNode(Opcodes.IFEQ, ifNotCanceled));
		toInject.add(new InsnNode(Opcodes.RETURN));
		toInject.add(ifNotCanceled);

		method.instructions.insertBefore(node, toInject);
	}

	private void injectSimpleHook(MethodNode method, AbstractInsnNode node, String hookClassName, String hookMethodName, String hookMethodDescriptor, InsnList loadParameters)
	{
		InsnList toInject = new InsnList();
		toInject.add(loadParameters);
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInternalClassName(hookClassName), hookMethodName, hookMethodDescriptor, false));

		method.instructions.insertBefore(node, toInject);
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{this.getClass().getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		isObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

	private ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	private byte[] writeClassToBytes(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && method.desc.equals(methodDesc))
			{
				return method;
			}
		}
		return null;
	}

	private static String toInternalClassName(String className)
	{
		return className.replace('.', '/');
	}

	private static boolean isDescriptor(String descriptor)
	{
		return descriptor.length() == 1 || (descriptor.startsWith("L") && descriptor.endsWith(";"));
	}

	private static String toDescriptor(String className)
	{
		return isDescriptor(className) ? className : "L" + toInternalClassName(className) + ";";
	}

	private static String toMethodDescriptor(String returnType, String... paramTypes)
	{
		StringBuilder paramDescriptors = new StringBuilder();
		for (String paramType : paramTypes)
			paramDescriptors.append(toDescriptor(paramType));

		return "(" + paramDescriptors.toString() + ")" + toDescriptor(returnType);
	}

	private static boolean isLabelOrLineNumber(AbstractInsnNode insn)
	{
		return insn.getType() == AbstractInsnNode.LABEL || insn.getType() == AbstractInsnNode.LINE;
	}

	private static AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (!isLabelOrLineNumber(instruction))
				return instruction;
		}
		return null;
	}

	private static AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		return getOrFindInstruction(method.instructions.getFirst(), false);
	}

	public static AbstractInsnNode getOrFindInstructionWithOpcode(AbstractInsnNode firstInsnToCheck, int opcode, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction.getOpcode() == opcode)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode getOrFindMethodInsn(AbstractInsnNode firstInsnToCheck, String methodName, String methodDesc, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).name.equals (methodName) && ((MethodInsnNode) instruction).desc.equals (methodDesc))
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode findLastInstructionWithOpcode(MethodNode method, int opcode)
	{
		return getOrFindInstructionWithOpcode(method.instructions.getLast(), opcode, true);
	}
}
