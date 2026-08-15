package com.ironmod.item;

/**
 * Define os tipos de módulo que podem ser instalados nas peças de armadura
 * através da Bancada de Montagem.
 *
 * Cada módulo ocupa um "slot" específico da peça (ex: bota só aceita
 * PROPULSION, peitoral aceita REPULSOR e PROJECTILE_CALL, etc).
 */
public enum ModuleType {
    // Módulo de propulsão: dá voo (like jato nas botas)
    PROPULSION("propulsion", "Propulsor"),

    // Módulo repulsor: dispara um projétil de dano pela mão/peitoral
    REPULSOR("repulsor", "Repulsor"),

    // Módulo de blindagem extra: mais armadura/toughness
    ARMOR_PLATING("armor_plating", "Blindagem Reforçada"),

    // Módulo de chamada: faz a armadura voar até o jogador (estilo Mark 7)
    PROJECTILE_CALL("projectile_call", "Chamada Automática"),

    // Módulo de absorção de impacto: reduz dano de queda
    SHOCK_ABSORBER("shock_absorber", "Amortecedor"),

    // Módulo de visão noturna no capacete
    NIGHT_VISION("night_vision", "Visão Noturna HUD"),

    // Módulo de nanotecnologia: escudo de energia + auto-reparo + voo mais estável
    NANOTECH("nanotech", "Nanotecnologia"),

    // Feixe carregado (Unibeam): segura clique direito pra carregar, solta pra disparar um feixe forte
    CHARGED_BEAM("charged_beam", "Feixe Carregado (Unibeam)"),

    // Imunidade a fogo
    FIRE_IMMUNITY("fire_immunity", "Imunidade a Fogo"),

    // Imunidade/redução de dano de projéteis
    PROJECTILE_IMMUNITY("projectile_immunity", "Defesa Antiprojétil"),

    // Respiração debaixo d'água
    WATER_BREATHING("water_breathing", "Respiração Aquática"),

    // Pele de metal: resistência a knockback e a efeitos de veneno/wither
    METAL_SKIN("metal_skin", "Pele de Metal"),

    // Escudo de energia: barra extra de "vida" que absorve dano e recarrega com o tempo
    SHIELD("shield", "Escudo de Energia"),

    // Modo Sentinela: modo de combate pesado, buffs de força/resistência por tempo limitado
    SENTINEL_MODE("sentinel_mode", "Modo Sentinela");

    private final String id;
    private final String displayName;

    ModuleType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ModuleType fromId(String id) {
        for (ModuleType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }
}
