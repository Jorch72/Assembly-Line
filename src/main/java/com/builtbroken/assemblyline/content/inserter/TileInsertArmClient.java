package com.builtbroken.assemblyline.content.inserter;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.client.FakeItemRender;
import com.builtbroken.assemblyline.content.rail.powered.TilePowerRailClient;
import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.core.References;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.rotation.EulerAngle;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.render.model.loader.EngineModelLoader;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

/**
 * Client side handling for insert arm
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 11/9/2016.
 */
public class TileInsertArmClient extends TileInsertArm implements ISimpleItemRenderer
{
    private static IModelCustom model = EngineModelLoader.loadModel(new ResourceLocation(AssemblyLine.DOMAIN, References.MODEL_PATH + "bot.arm.classic.tcn"));
    private static ResourceLocation texture = new ResourceLocation(AssemblyLine.DOMAIN, "textures/models/metal.png");
    private ItemStack renderStack;
    private EulerAngle renderAngle = new EulerAngle(0,0,0);

    private static IIcon[] arrow;

    @Override
    public Tile newTile()
    {
        return new TileInsertArmClient();
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        rotation.readByteBuf(buf);
        if (buf.readBoolean())
        {
            renderStack = ByteBufUtils.readItemStack(buf);
        }
        else
        {
            renderStack = null;
        }
        worldObj.markBlockRangeForRenderUpdate(xi(), yi(), zi(), xi(), yi(), zi());
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        GL11.glScaled(.8f, .8f, .8f);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        model.renderAll();
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(0, 0, 0, 1, 2, 1).add(x(), y(), z()).toAABB();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float deltaFrame, int pass)
    {
        if(ticks < 2)
        {
            renderAngle.set(rotation);
        }
        else
        {
            renderAngle.lerp(rotation, deltaFrame).clampTo360();
        }
        float yaw = (float)renderAngle.yaw();

        //Render inserter
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.xf() + 0.5f, pos.yf() + 0.4f, pos.zf() + 0.5f);
        GL11.glRotated(yaw, 0, 1, 0);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        model.renderAllExcept("Base", "BaseTop");
        GL11.glPopMatrix();

        //-----------------------------
        //Render item in hand
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xf() + 0.5f, pos.yf() + 0.2f, pos.zf() + 0.5f);

        //Setup world position
        double x = renderAngle.x() * 1.15;
        double z = renderAngle.z() * 1.15;
        FakeItemRender.setWorldPosition(worldObj, xi() + 0.5 + x, yi() + 0.5, zi() + 0.5 + z);

        //Render actual item with rotation
        yaw += 90f;
        GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
        FakeItemRender.renderItemAtPosition(1, 0, 0, renderStack);
        GL11.glPopMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (side == 1 && getFacing() != null)
        {
            if (getFacing() == ForgeDirection.NORTH)
            {
                return arrow[0];
            }
            else if (getFacing() == ForgeDirection.EAST)
            {
                return arrow[3];
            }
            else if (getFacing() == ForgeDirection.SOUTH)
            {
                return arrow[1];
            }
            else if (getFacing() == ForgeDirection.WEST)
            {
                return arrow[2];
            }
        }
        return TilePowerRailClient.main;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        final String[] sideName = new String[]{"Up", "Down", "Left", "Right"};

        arrow = new IIcon[4];
        for (int i = 0; i < sideName.length; i++)
        {
            arrow[i] = iconRegister.registerIcon(AssemblyLine.PREFIX + "SimpleArrow" + sideName[i]);
        }
    }
}
