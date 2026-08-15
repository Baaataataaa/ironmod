# Iron Suit Mod (Forge 1.20.1)

Mod de armadura modular estilo Homem de Ferro: bancada de montagem +
módulos instaláveis + item que "chama" a armadura até você (estilo Mark 7).

## O que já está pronto (código funcional)

- **Bancada de Montagem** (`ironmod:assembly_bench`): bloco craftável com
  ferro + diamante. Botão direito abre uma GUI com 3 slots: peça de
  armadura, módulo, resultado.
- **4 peças de armadura modular** (capacete, peitoral, calças, botas),
  cada uma com "slots" de módulo diferentes (definidos em
  `ModularArmorItem.ALLOWED_MODULES`).
- **6 módulos**, cada um com efeito real via `ModuleEffectHandler`:
  - **Propulsão** (botas): permite voo.
  - **Repulsor** (peitoral): Shift + clique direito no ar dispara um
    "raio" que causa dano em cone na frente do jogador.
  - **Blindagem Reforçada**: módulo cosmético/preparado pra você
    expandir com atributos extras de defesa.
  - **Chamada Automática** (peitoral): necessário para usar o
    Dispositivo de Chamada.
  - **Amortecedor**: zera o dano de queda.
  - **Visão Noturna** (capacete): aplica o efeito continuamente.
- **Dispositivo de Chamada da Armadura** (`mark_call_device`): item
  craftável. Ao usar, procura as 4 peças no seu inventário, exige que a
  peitoral tenha o módulo "Chamada Automática", remove as peças do
  inventário e invoca um projétil (`SummonedArmorProjectile`) que voa
  até você (acelerando com o tempo, tipo a cena da Mark 7) e equipa
  tudo automaticamente ao chegar perto.
- Receitas de crafting pra todos os itens (em `data/ironmod/recipes`).

## O que falta você adicionar (não consegui gerar aqui)

Eu não tenho acesso à internet neste ambiente, então não consegui:

1. **Baixar o Forge MDK / rodar o Gradle** para compilar de fato o
   `.jar`. Você vai precisar rodar isso na sua máquina.
2. **Texturas**: os modelos de item/bloco já apontam pros caminhos
   certos (`textures/item/*.png`, `textures/block/assembly_bench.png`),
   mas os PNGs em si (16x16 pra itens, 16x16 pra bloco) não existem
   ainda. Sem eles o jogo vai mostrar o bloco roxo/preto de "textura
   faltando". A tela da bancada também está com um fundo cinza simples
   (fallback) em vez de uma imagem `.png` de GUI (176x166).

## Como compilar

1. Instale o **JDK 17**.
2. Baixe o **Forge MDK 1.20.1-47.2.0** em
   https://files.minecraftforge.net/ e extraia por cima desta pasta
   (ou copie os arquivos `build.gradle`/`settings.gradle`/código-fonte
   pra dentro do MDK baixado — o MDK já vem com o `gradlew`).
3. No terminal, dentro da pasta do projeto:
   ```
   ./gradlew build
   ```
   (no Windows: `gradlew.bat build`)
4. O `.jar` final aparece em `build/libs/ironmod-1.0.0.jar`. Copie pra
   pasta `mods` da sua instância Forge 1.20.1.
5. Pra testar direto sem gerar o jar: `./gradlew runClient`.

## Estrutura do projeto

```
src/main/java/com/ironmod/
  IronMod.java                  -> classe principal, registra tudo
  item/
    ModuleType.java             -> enum dos tipos de módulo
    ArmorModuleItem.java        -> item físico do módulo
    ModularArmorItem.java       -> peça de armadura com módulos em NBT
    ModArmorMaterials.java      -> material de armadura (defesa/durabilidade)
    MarkCallItem.java           -> item "chamar armadura"
  block/
    AssemblyBenchBlock.java     -> bloco da bancada
  menu/
    AssemblyBenchMenu.java      -> lógica dos slots da bancada
  client/screen/
    AssemblyBenchScreen.java    -> GUI da bancada
  entity/
    SummonedArmorProjectile.java -> projétil que voa até o jogador
    ModEntities.java
  event/
    ModuleEffectHandler.java    -> voo, queda, repulsor, visão noturna
  registry/
    ModItems.java, ModBlocks.java, ModMenuTypes.java, ModCreativeTabs.java
```

## Ideias pra você expandir depois

- Dar um modelo 3D customizado pra armadura (capacete tipo Iron Man)
  usando um `ArmorItem` com layer de textura em
  `textures/models/armor/iron_suit_layer_1.png` /
  `_layer_2.png` (padrão vanilla de textura de armadura).
- Um HUD customizado (tipo o visor do Homem de Ferro) usando
  `RenderGuiOverlayEvent`.
- Um sistema de "energia/bateria" pro traje, consumido pelo repulsor e
  pelo voo, recarregável na própria bancada.
- Renderizar o `SummonedArmorProjectile` como um modelo 3D da armadura
  voando (hoje ele só solta partículas).
