This package should contain at least one mixin for each vanilla implementation of net.minecraft.world.entity.TraceableEntity.getOwner()

### Extra things to account for
- VexCopyOwnerTargetGoalMixin directly accesses the owner field, requiring a mixin
- Interdimensional owner lookup of enderpearls

### Not needed for
- EvokerFangs, as it does not copy the owner to another instance in Entity.restoreFrom (cross dimension teleport of the evoker fangs)
- AreaEffectCloud, similar to EvokerFangs
