package net.liukrast.santa;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.liukrast.santa.client.renderer.entity.SleighRenderer;
import net.liukrast.santa.registry.SantaBlockEntityTypes;
import net.liukrast.santa.registry.SantaEntityTypes;
import net.liukrast.santa.registry.SantaMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
@Mod(value = SantaConstants.MOD_ID, dist = Dist.CLIENT)
public class SantaClient {

    public SantaClient(IEventBus eventBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        eventBus.addListener(SantaEntityTypes::registerLayerDefinitions);
        eventBus.addListener(SantaEntityTypes::registerRenderers);
        eventBus.addListener(SantaBlockEntityTypes::registerRenderers);
        eventBus.addListener(SantaBlockEntityTypes::fmlClientSetup);
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new BaseConfigScreen(parent, modContainer.getModId()));
        eventBus.addListener(SantaMenuTypes::registerMenuScreens);
        eventBus.addListener(this::fMLClientSetup);
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent.Pre event) {
        if((SleighRenderer.DOCKS != null || SleighRenderer.ORIGIN != null) && Minecraft.getInstance().level == null) {
            SleighRenderer.DOCKS = null;
            SleighRenderer.ORIGIN = null;
        }
    }

    @SubscribeEvent
    public void renderLevelStage(RenderLevelStageEvent event) {
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0, -1.625, 0);
        MultiBufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        var level = Minecraft.getInstance().level;
        assert level != null;
        float smooth = level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).get() ? event.getPartialTick().getGameTimeDeltaPartialTick(true) : 0;
        SleighRenderer.renderSleigh(level.dayTime() % 24000 + smooth, poseStack, source);
        for(int i = 0; i < 3; i++) {
            SleighRenderer.renderRudolf(level.dayTime() % 24000 + smooth + 200*(i) + 400, poseStack, source, i);
        }
        poseStack.popPose();
    }

    private void fMLClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new SantaPonderPlugin());
    }
}
