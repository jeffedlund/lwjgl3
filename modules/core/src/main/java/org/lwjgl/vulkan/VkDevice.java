/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package org.lwjgl.vulkan;

import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VkInstance.*;

/** Wraps a Vulkan device dispatchable handle. */
public class VkDevice extends DispatchableHandle {

	/**
	 * Creates a {@link VkDevice} instance for the specified native handle.
	 *
	 * @param handle         the native {@code VkDevice} handle
	 * @param physicalDevice the physical device used to create the {@code VkDevice}
	 * @param ci             the {@link VkDeviceCreateInfo} structure used to create the {@code VkDevice}
	 */
	public VkDevice(long handle, VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci) {
		super(handle, getDeviceCapabilities(handle, physicalDevice, ci));
	}

	private static VKCapabilities getDeviceCapabilities(final long handle, final VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci) {
		int apiVersion = physicalDevice.getCapabilities().apiVersion;
		return new VKCapabilities(
			new FunctionProvider() {
				@Override
				public long getFunctionAddress(CharSequence functionName) {
					MemoryStack stack = stackPush();
					try {
						long nameEncoded = memAddress(memEncodeASCII(functionName, true, BufferAllocator.STACK));

						VKCapabilities caps = physicalDevice.getCapabilities();
						long address = GetDeviceProcAddr(caps.vkGetDeviceProcAddr, handle, nameEncoded);
						if ( address == NULL ) {
							address = GetInstanceProcAddr(caps.vkGetInstanceProcAddr, physicalDevice.getInstance().address(), nameEncoded);
							if ( address == NULL )
								address = VK.getFunctionProvider().getFunctionAddress(functionName);
						}

						return address;
					} finally {
						stack.pop();
					}
				}

				@Override
				public void free() {
				}
			}, apiVersion, VK.getEnabledExtensionSet(apiVersion, ci.ppEnabledExtensionNames()));
	}

	static long GetDeviceProcAddr(long __functionAddress, long handle, long functionName) {
		return callPPP(__functionAddress, handle, functionName);
	}

}