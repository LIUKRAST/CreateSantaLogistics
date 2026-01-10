package net.liukrast.santa.client.gui.screens;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import net.liukrast.santa.SantaConstants;
import net.liukrast.santa.network.protocol.game.SantaDockConfirmInfoPacket;
import net.liukrast.santa.world.inventory.SantaDockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SantaDockScreen extends AbstractSimiContainerScreen<SantaDockMenu> {
    private static final ResourceLocation TEXTURE = SantaConstants.id("textures/gui/santa_dock.png");
    private EditBox editBox;

    public SantaDockScreen(SantaDockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 185;
        imageWidth = 192;
    }

    @Override
    protected void init() {
        super.init();
        String old = editBox == null ? menu.address : editBox.getValue();
        editBox = new EditBox(font, leftPos+28, topPos+68, 130, 20, Component.empty());
        assert old != null;
        editBox.setValue(old);
        editBox.setMaxLength(100);
        editBox.setTextColor(0xFF545454);
        editBox.setTextShadow(false);
        editBox.setBordered(false);
        IconButton confirmButton = new IconButton(leftPos+imageWidth-25, topPos+87-24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            PacketDistributor.sendToServer(new SantaDockConfirmInfoPacket(editBox.getValue(), menu.pos));
            Minecraft.getInstance().setScreen(null);
        });
        confirmButton.setToolTip(CreateLang.translate("gui.factory_panel.save_and_close").component());
        addRenderableWidget(confirmButton);
        addRenderableWidget(editBox);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, leftPos+imageWidth/2, topPos + 4, 0x3D3C48);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 200, 185);
        /*GuiGameElement.of(SantaBlocks.SANTA_DOCK.toStack())
                .scale(4)
                .at(0, 0, -200)
                .render(graphics, leftPos + imageWidth, topPos+imageHeight-48);*/
    }
}
