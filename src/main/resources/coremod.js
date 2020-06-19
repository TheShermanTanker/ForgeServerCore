function initializeCoreMod() {
	return {
		"Coremod": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.item.CrossbowItem",
				"methodName": "func_200887_a",
				"methodDesc": "()Lnet/minecraft/item/CrossbowItem;"
			},
			"transformer": function(methodNode) {
				// Apply transformations
				return methodNode;
			}
		}
	}
}