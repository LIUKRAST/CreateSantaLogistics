package net.liukrast.santa.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liukrast.santa.SantaConstants;
import net.liukrast.santa.world.entity.SantaClaus;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.lwjgl.system.NonnullDefault;

@NonnullDefault
public class SantaClausModel extends EntityModel<SantaClaus> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(SantaConstants.id("santa_claus"), "main");

    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hat_1;
    private final ModelPart hat_2;
    private final ModelPart hat_3;
    private final ModelPart beard;
    private final ModelPart right_mustache;
    private final ModelPart left_mustache;
    private final ModelPart goggles;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public SantaClausModel(ModelPart root) {
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hat_1 = this.hat.getChild("hat_1");
        this.hat_2 = this.hat_1.getChild("hat_2");
        this.hat_3 = this.hat_2.getChild("hat_3");
        this.beard = this.head.getChild("beard");
        this.right_mustache = this.head.getChild("right_mustache");
        this.left_mustache = this.head.getChild("left_mustache");
        this.goggles = this.head.getChild("goggles");
        this.right_arm = this.chest.getChild("right_arm");
        this.left_arm = this.chest.getChild("left_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 61).addBox(-11.0F, -26.0F, -11.0F, 22.0F, 14.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(11.0F, -21.0F, -8.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(8, 24).addBox(11.0F, -17.0F, -7.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 24).addBox(11.0F, -17.0F, -2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 24).addBox(11.0F, -20.0F, -4.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(8, 24).addBox(11.0F, -17.0F, 2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 24).addBox(11.0F, -21.0F, 2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).mirror().addBox(-12.0F, -21.0F, -8.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(8, 24).mirror().addBox(-12.0F, -17.0F, -7.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(8, 24).mirror().addBox(-12.0F, -17.0F, -2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 24).mirror().addBox(-12.0F, -20.0F, -4.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(8, 24).mirror().addBox(-12.0F, -17.0F, 2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 24).mirror().addBox(-12.0F, -21.0F, 2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 2.0F));

        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(2, 39).addBox(-10.0F, -9.0F, -8.0F, 20.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -25.0F, 0.0F));

        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -12.0F, -7.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -9.0F, -9.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(66, 6).addBox(-7.0F, -3.0F, -8.0F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition hat_1 = hat.addOrReplaceChild("hat_1", CubeListBuilder.create().texOffs(56, 24).addBox(-6.0F, -7.0F, -7.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hat_2 = hat_1.addOrReplaceChild("hat_2", CubeListBuilder.create().texOffs(56, 40).addBox(-4.0F, -6.0F, -5.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        hat_2.addOrReplaceChild("hat_3", CubeListBuilder.create().texOffs(96, 32).addBox(-1.0F, -1.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(48, 8).addBox(-6.0F, -1.0F, -2.0F, 12.0F, 8.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -5.0F, -6.0F));

        head.addOrReplaceChild("right_mustache", CubeListBuilder.create().texOffs(0, 8).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -6.0F, -8.0F));

        head.addOrReplaceChild("left_mustache", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(0.0F, -1.0F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, -6.0F, -8.0F));

        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(36, 0).addBox(-7.0F, -2.0F, -1.0F, 14.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -7.0F));

        chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(88, 48).addBox(0.0F, -4.0F, -3.0F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(88, 78).addBox(0.0F, -4.0F, -3.0F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(10.0F, -5.0F, 0.0F));

        chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(88, 48).mirror().addBox(-6.0F, -4.0F, -3.0F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(88, 78).mirror().addBox(-6.0F, -4.0F, -3.0F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(-10.0F, -5.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 107).addBox(3.0F, -6.0F, -9.0F, 8.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 93).addBox(3.0F, -12.0F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 93).addBox(3.0F, -12.0F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 107).mirror().addBox(-11.0F, -6.0F, -9.0F, 8.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 93).mirror().addBox(-11.0F, -12.0F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(32, 93).mirror().addBox(-11.0F, -12.0F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.right_arm.translateAndRotate(poseStack);
        poseStack.translate(0, 0.3, 0);
    }

    public void setupAnimForSleigh(float pitch, float yaw, float ageInTicks) {
        body.yRot=yaw;
        body.xRot = pitch;

        hat.xRot = -0.1f + Mth.sin(ageInTicks*.1f)*.01f;
        hat.yRot = -0.1f;
        hat.zRot = 0.1f;

        hat_1.yRot = -0.2f + Mth.sin(ageInTicks*.1f)*.05f;

        hat_2.yRot = -0.2f + Mth.sin(ageInTicks*.1f)*.05f;
        hat_2.zRot = 0.2f;

        hat_3.yRot = -0.5f;
        hat_3.zRot = -0.3f;
        hat_3.xRot = Mth.cos(ageInTicks*.1f)*.1f;

        beard.xRot = -0.2f;

        right_mustache.xRot = -0.1f;
        right_mustache.yRot = -0.2f;
        right_mustache.zRot = -0.2f;

        left_mustache.xRot = -0.1f;
        left_mustache.yRot = 0.2f;
        left_mustache.zRot = 0.2f;

        right_arm.zRot = -0.1f;
        left_arm.zRot = 0.1f;
        right_arm.yRot = 0.5f;
        left_arm.yRot = -0.5f;
        right_arm.xRot = -1 + Mth.sin(ageInTicks*.1f)*.01f;
        left_arm.xRot = right_arm.xRot;

        right_leg.z = 0.5f;
        right_leg.y = 24;
        left_leg.z = 0.5f;
        left_leg.y = 24;

        body.y = 24 + Mth.sin(ageInTicks*.1f)*0.1f;
        right_arm.y = -5;
        left_arm.y = right_arm.y;
        body.xRot = 0;

        body.y = 6;
        right_leg.visible = false;
        left_leg.visible = false;
    }

    @Override
    public void setupAnim(SantaClaus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        /* RESET */
        beard.resetPose();
        right_arm.resetPose();
        left_arm.resetPose();
        right_leg.resetPose();
        left_leg.resetPose();
        chest.resetPose();
        /* ALL POSES */
        hat.xRot = -0.1f + Mth.cos(limbSwing)*limbSwingAmount*0.05f + Mth.sin(ageInTicks*.1f)*.01f;
        hat.yRot = -0.1f;
        hat.zRot = 0.1f;

        hat_1.yRot = -0.2f + Mth.sin(ageInTicks*.1f)*.05f;

        hat_2.yRot = -0.2f + Mth.sin(ageInTicks*.1f)*.05f;
        hat_2.zRot = 0.2f + Mth.sin(limbSwing)*limbSwingAmount*0.1f;

        hat_3.yRot = -0.5f;
        hat_3.zRot = -0.3f + Mth.cos(limbSwing)*limbSwingAmount*0.1f;
        hat_3.xRot = Mth.cos(ageInTicks*.1f)*.1f;

        beard.xRot = -0.2f;

        right_mustache.xRot = -0.1f;
        right_mustache.yRot = -0.2f;
        right_mustache.zRot = -0.2f;

        left_mustache.xRot = -0.1f;
        left_mustache.yRot = 0.2f;
        left_mustache.zRot = 0.2f;

        head.yRot = netHeadYaw * (float) (Math.PI / 180.0) / 2;
        chest.yRot = netHeadYaw * (float) (Math.PI / 180.0) / 2;

        head.xRot = headPitch * (float) (Math.PI / 180.0);

        right_arm.zRot = -0.1f;
        left_arm.zRot = 0.1f;
        right_arm.yRot = 0.5f;
        left_arm.yRot = -0.5f;
        right_arm.xRot = -Mth.abs(Mth.cos(limbSwing)*limbSwingAmount*0.3f)-1 + Mth.sin(ageInTicks*.1f)*.01f;
        left_arm.xRot = right_arm.xRot;

        right_leg.xRot = -Mth.clamp(-Mth.cos(limbSwing),0,1)*limbSwingAmount;
        right_leg.z = Mth.cos(limbSwing)*limbSwingAmount*8f+0.5f;
        right_leg.y = 24-Mth.sin(limbSwing)*limbSwingAmount*4f;
        left_leg.xRot = -Mth.clamp(Mth.cos(limbSwing)*limbSwingAmount,0,1);
        left_leg.z = -Mth.cos(limbSwing)*limbSwingAmount*8f+0.5f;
        left_leg.y = 24+Mth.sin(limbSwing)*limbSwingAmount*4f;

        body.y = 24-Mth.abs(Mth.sin(limbSwing))*limbSwingAmount*2 + Mth.sin(ageInTicks*.1f)*0.1f;
        right_arm.y = -5-Mth.abs(Mth.cos(limbSwing))*limbSwingAmount -Mth.abs(Mth.sin(limbSwing))*limbSwingAmount;
        left_arm.y = right_arm.y;
        body.xRot = 0;



        float animationTime = ageInTicks-entity.animationTime;
        if(entity.lastFoundState != entity.getAnimationState()) {
            entity.lastFoundState = entity.getAnimationState();
            entity.animationTime = ageInTicks;
        }
        switch (entity.lastFoundState) {
            case IDLE -> {
            }
            case CURIOUS -> {
                right_arm.xRot = -4;
                right_arm.yRot = -1.3f - head.xRot;

                right_arm.x = Mth.cos(ageInTicks/2) + 10;
                right_arm.y = Mth.cos(ageInTicks/2) - 10;
            }
            case ANGRY -> {
                right_mustache.xRot = -0.1f;
                right_mustache.yRot = -0.2f;
                right_mustache.zRot = 0.3f + Mth.cos(ageInTicks*3)*0.1f;


                left_mustache.xRot = -0.1f;
                left_mustache.yRot = 0.2f;
                left_mustache.zRot = -0.3f + Mth.cos(ageInTicks*3)*0.1f;

                float backTo = (1-Mth.cos(animationTime*.05f*Mth.PI))/2;

                body.xRot = animationTime > 20 ? 0 : -(1-Mth.cos(animationTime*Mth.PI*0.1f))*0.1f;
                chest.xRot = animationTime > 20 ? 0 : -(1-Mth.cos(animationTime*Mth.PI*0.1f))*0.1f;
                right_arm.xRot = animationTime > 20 ? right_arm.xRot : -Mth.sin(animationTime*Mth.PI*0.1f)*1.5f + backTo*right_arm.xRot;
                right_arm.zRot = animationTime > 20 ? right_arm.zRot : -(1-Mth.cos(animationTime*Mth.PI*0.1f))*0.7f + backTo*right_arm.zRot;

                left_arm.xRot = animationTime > 20 ? left_arm.xRot : -Mth.sin(animationTime*Mth.PI*0.1f)*1.5f + backTo*left_arm.xRot;
                left_arm.zRot = animationTime > 20 ? left_arm.zRot : (1-Mth.cos(animationTime*Mth.PI*0.1f))*0.7f + backTo*left_arm.zRot;

                beard.y = -3;
            }
            case EATING -> {
                right_arm.xRot = right_arm.xRot - 1 + Mth.cos(ageInTicks)*0.3f;
                beard.y += Mth.sin(ageInTicks);
            }
            case LOOTING -> {
                right_arm.xRot = right_arm.xRot+ Mth.cos(ageInTicks*0.2f)*0.1f;
                right_arm.yRot = right_arm.yRot + Mth.sin(ageInTicks*0.5f)*0.1f;
                left_arm.xRot = left_arm.xRot+ Mth.cos(ageInTicks*0.2f)*0.1f;
                left_arm.yRot = left_arm.yRot - Mth.sin(ageInTicks*0.5f)*0.1f;
            }
            case SLEEPING -> {
                body.y+=8;
                body.xRot-=0.5f;

                right_leg.xRot-=Mth.PI/2;
                left_leg.xRot-=Mth.PI/2;
                right_leg.zRot-=0.3f;
                left_leg.zRot+=0.3f;
                right_leg.yRot+=0.5f;
                left_leg.yRot-=0.5f;

                right_leg.z-=12;
                left_leg.z-=12;
                right_leg.y-=6;
                left_leg.y-=6;
                right_leg.x-=6;
                left_leg.x+=6;

                chest.xRot+=0.1f;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        left_leg.render(poseStack, buffer, packedLight, packedOverlay, color);
        right_leg.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
