package com.ironmod.client.renderer;

import com.ironmod.entity.SummonedArmorProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer "vazio" pro projétil que carrega a armadura até o jogador.
 * A entidade não precisa de um modelo visível (o efeito já é feito com
 * partículas emitidas no tick dela em SummonedArmorProjectile) — mas o
 * Forge exige que TODA entidade tenha um EntityRenderer registrado no
 * cliente, senão o jogo trava (NullPointerException) na hora de tentar
 * desenhar ela na tela.
 */
public class SummonedArmorProjectileRenderer extends EntityRenderer<SummonedArmorProjectile> {

    public SummonedArmorProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SummonedArmorProjectile entity) {
        // Nunca é usado de fato pra desenhar nada, mas o método é obrigatório.
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
