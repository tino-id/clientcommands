package net.earthcomputer.clientcommands.mixin.rngevents;

import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Creeper.class, MushroomCow.class, Sheep.class, SnowGolem.class})
public class CreeperMushroomCowSheepAndSnowGolemMixin {

    @Inject(method = "mobInteract", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    public void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<Boolean> ci) {
        PlayerRandCracker.onItemDamage(1, player, player.getItemInHand(hand));
    }

}
