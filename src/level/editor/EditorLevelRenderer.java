package level.editor;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import foundation.math.ObjPos;
import level.AbstractLevelRenderer;
import level.LevelEditor;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileType;
import render.*;
import render.level.map.LevelMapUI;
import render.level.tile.HexagonRenderer;
import render.level.tile.RenderElement;
import render.save.UISaveMenu;
import render.texture.ImageRenderer;
import render.types.box.UIBox;
import render.types.box.UIShapeDisplayBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.container.UITabSwitcher;
import render.types.box.UIClickBlockingBox;
import render.types.input.UIEnumSelector;
import render.types.input.UITextInputBox;
import render.types.input.button.LevelUIButton;
import render.types.input.button.LevelUIShapeButton;
import render.types.input.button.UIButton;
import render.types.input.button.UIShapeButton;
import render.types.text.*;
import save.MapSave;
import unit.NeutralUnitTeam;
import unit.UnitPose;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import static render.types.text.UITextLabel.*;

public class EditorLevelRenderer extends AbstractLevelRenderer<LevelEditor> {
    private final ArrayList<UIButton> tileButtons = new ArrayList<>();
    private final ArrayList<UIButton> unitButtons = new ArrayList<>();
    private final ArrayList<UIButton> structureButtons = new ArrayList<>();
    private final ArrayList<UITeamDisplay> unitDisplays = new ArrayList<>(), structureDisplays = new ArrayList<>();

    private UITabSwitcher tabSwitcher;
    private TileType editingTileType = null;
    private UnitType editingUnitType = null;
    private StructureType editingStructureType = null;
    private boolean removeUnit = false;
    private boolean removeStructure = false;
    public NeutralUnitTeam editingTeam = NeutralUnitTeam.fromTeam(UnitTeam.ORDERED_TEAMS[0]);
    private UISaveMenu<MapSave> saveMenu;
    private UIEnumSelector<NeutralUnitTeam> teamSelector;
    private LevelUIContainer<LevelEditor> invalidContainer;
    private final MultiLineTextBox invalidText = new MultiLineTextBox(1, 2.15f, 8, 0.7f, TextAlign.LEFT);
    public LevelMapUI mapUI;
    public UIConfirm confirm;

    public EditorLevelRenderer(LevelEditor level) {
        super(level);
    }

    @Override
    public void createRenderers() {
        super.createRenderers();
        confirm = new UIConfirm(levelUIRenderer, RenderOrder.CONFIRM_UI, level)
                .setTextSize(0.7f).setTextConfirm("Back").setTextCancel("Exit");
        level.buttonRegister.register(confirm);
        new LevelUIButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 0.5f, Renderable.top() - 2.5f, 8, 2, 1.4f, false, level, () -> {
            if (level.unsaved)
                confirm.makeVisible("Map has unsaved work. Exit to main menu without saving?", () -> confirm.makeInvisible(), () -> MainPanel.addTask(MainPanel::toTitleScreen));
            else
                MainPanel.addTask(MainPanel::toTitleScreen);
        }).setText("Exit").setColourTheme(UIColourTheme.DEEP_RED).setBold();
        new LevelUIContainer<>(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                44, 4, level).addRenderables((r, b) -> {
            tabSwitcher = new UITabSwitcher(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                    0, 0, 14, Renderable.top() - 8)
                    .addTab(3, "Tiles", (r2, b2) -> {
                        new UIElementScrollSurface<>(r2, b2, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                0, 0, 14, Renderable.top() - 8, false, size -> size * 5f + 0.5f)
                                .addElements(TileType.values().length, (r3, b3, i) -> new UIContainer(r3, b3, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                        1, -5 * (i + 1)).addRenderables((r4, b4) -> {
                                    TileType type = TileType.values()[i];
                                    tileButtons.add(new UIButton(r4, b4, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                            0, 0, 12, 4.5f, 0, true).setColourTheme(UIColourTheme.GREEN_SELECTED_OPAQUE_CENTER).noDeselect().setOnClick(() -> {
                                        for (int j = 0; j < tileButtons.size(); j++) {
                                            if (i != j)
                                                tileButtons.get(j).deselect();
                                        }
                                        level.tileSelector.deselect();
                                        editingTileType = type;
                                    }));
                                    if (type.tileTextures != null) {
                                        ImageRenderer image = type.tileTextures.getImage(0);
                                        new RenderElement(r4, RenderOrder.LEVEL_UI,
                                                g -> {
                                                    GameRenderer.renderOffset(9.5f, 4.5f / 2, g, () -> {
                                                        image.render(g, 4);
                                                    });
                                                }).setZOrder(1);
                                    }
                                    Renderable hexagonRenderer = new HexagonRenderer(4, false, 0.2f, new Color(117, 117, 117, 200)).translate(9.5f, 4.5f / 2 - 4 / 2f * Tile.SIN_60_DEG);
                                    new RenderElement(r4, RenderOrder.LEVEL_UI, hexagonRenderer,
                                            new UITextLabel(6, 0.7f, false).setTextCenterBold()
                                                    .updateTextCenter(type.displayName + " tile").translate(0.5f, 3.3f)).setZOrder(2);
                                })).addScrollBar(0.7f, 0.3f, -0.4f);
                    }).addTab(3, "Units", (r2, b2) -> {
                        new UIElementScrollSurface<>(r2, b2, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                0, 0, 14, Renderable.top() - 8, false, size -> size * 5f + 0.5f)
                                .addElements(UnitType.ORDERED_UNIT_TYPES.length + 1, (r3, b3, i) -> {
                                    UnitType type = i == 0 ? null : UnitType.ORDERED_UNIT_TYPES[i - 1];
                                    UITeamDisplay display = new UITeamDisplay(r3, b3, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                            1, -5 * (i + 1), () -> {
                                        unitButtons.get(i).setColourTheme(editingTeam == NeutralUnitTeam.NEUTRAL ? UIColourTheme.GRAYED_OUT_OPAQUE : UIColourTheme.GREEN_SELECTED_OPAQUE_CENTER)
                                                .setClickEnabled(editingTeam != NeutralUnitTeam.NEUTRAL);
                                        if (editingTeam == NeutralUnitTeam.NEUTRAL) {
                                            unitButtons.forEach(UIButton::deselect);
                                        }
                                        if (type == null)
                                            return null;
                                        return ImageRenderer.renderImageCentered(type.getImage(editingTeam == NeutralUnitTeam.NEUTRAL ? UnitTeam.ORDERED_TEAMS[0] : editingTeam.unitTeam, UnitPose.INFO), true);
                                    });
                                    unitDisplays.add(display);
                                    return display.addRenderables((r4, b4) -> {
                                        unitButtons.add(new UIButton(r4, b4, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                                0, 0, 12, 4.5f, 0, true).setColourTheme(UIColourTheme.GREEN_SELECTED_OPAQUE_CENTER).noDeselect().setOnClick(() -> {
                                            for (int j = 0; j < unitButtons.size(); j++) {
                                                if (i != j) {
                                                    UIButton button = unitButtons.get(j);
                                                    if (button.isSelected())
                                                        button.deselect();
                                                }
                                            }
                                            level.tileSelector.deselect();
                                            editingUnitType = type;
                                            if (type == null)
                                                removeUnit = true;
                                        }).setOnDeselect(() -> {
                                            if (type == null)
                                                removeUnit = false;
                                        }));
                                        new RenderElement(r4, RenderOrder.LEVEL_UI,
                                                g -> {
                                                    GameRenderer.renderOffset(9.5f, 4.5f / 2, g, () -> {
                                                        if (display.image != null)
                                                            display.image.render(g, 4);
                                                    });
                                                }).setZOrder(1);
                                        new RenderElement(r4, RenderOrder.LEVEL_UI,
                                                new UITextLabel(6, 0.7f, false).setTextCenterBold()
                                                        .updateTextCenter(type == null ? "Remove unit" : type.getName()).translate(0.5f, 3.3f)).setZOrder(2);
                                        if (type == null)
                                            new UIShapeDisplayBox(r4, RenderOrder.LEVEL_UI, 12 - 3 - 1, 4.5f / 2 - 3 / 2f, 3, 3)
                                                    .setColourTheme(UIColourTheme.DEEP_RED).setShape(UIShapeButton::smallX).setZOrder(3);
                                    });
                                }).addScrollBar(0.7f, 0.3f, -0.4f);
                    }).addTab(4.5f, "Structures", (r2, b2) -> {
                        new UIElementScrollSurface<>(r2, b2, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                0, 0, 14, Renderable.top() - 8, false, size -> size * 5f + 0.5f)
                                .addElements(StructureType.values().length + 1, (r3, b3, i) -> {
                                    StructureType type = i == 0 ? null : StructureType.values()[i - 1];
                                    UITeamDisplay display = new UITeamDisplay(r3, b3, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                            1, -5 * (i + 1), () -> {
                                        if (type != null && !type.hasNeutral)
                                            structureButtons.get(i).setColourTheme(editingTeam == NeutralUnitTeam.NEUTRAL ? UIColourTheme.GRAYED_OUT_OPAQUE : UIColourTheme.GREEN_SELECTED_OPAQUE_CENTER)
                                                    .setClickEnabled(editingTeam != NeutralUnitTeam.NEUTRAL);
                                        if (editingTeam == NeutralUnitTeam.NEUTRAL) {
                                            for (int j = 1; j < structureButtons.size(); j++) {
                                                if (!StructureType.values()[j - 1].hasNeutral)
                                                    structureButtons.get(j).deselect();
                                            }
                                        }
                                        if (type == null)
                                            return null;
                                        return type.getLightImage(editingTeam.unitTeam);
                                    });
                                    structureDisplays.add(display);
                                    return display.addRenderables((r4, b4) -> {
                                        structureButtons.add(new UIButton(r4, b4, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                                                0, 0, 12, 4.5f, 0, true).setColourTheme(UIColourTheme.GREEN_SELECTED_OPAQUE_CENTER).noDeselect().setOnClick(() -> {
                                            for (int j = 0; j < structureButtons.size(); j++) {
                                                if (i != j)
                                                    structureButtons.get(j).deselect();
                                            }
                                            level.tileSelector.deselect();
                                            editingStructureType = type;
                                            if (type == null)
                                                removeStructure = true;
                                        }).setOnDeselect(() -> {
                                            if (type == null)
                                                removeStructure = false;
                                        }));
                                        new RenderElement(r4, RenderOrder.LEVEL_UI,
                                                g -> {
                                                    GameRenderer.renderOffset(9.5f, 4.5f / 2, g, () -> {
                                                        if (display.image != null)
                                                            display.image.render(g, 4);
                                                    });
                                                }).setZOrder(1);
                                        new RenderElement(r4, RenderOrder.LEVEL_UI,
                                                new UITextLabel(6, 0.7f, false).setTextCenterBold()
                                                        .updateTextCenter(type == null ? "Remove structure" : type.getName()).translate(0.5f, 3.3f)).setZOrder(2);
                                        if (type == null)
                                            new UIShapeDisplayBox(r4, RenderOrder.LEVEL_UI, 12 - 3 - 1, 4.5f / 2 - 3 / 2f, 3, 3)
                                                    .setColourTheme(UIColourTheme.DEEP_RED).setShape(UIShapeButton::smallX).setZOrder(3);
                                    });
                                }).addScrollBar(0.7f, 0.3f, -0.4f);
                    });
            NeutralUnitTeam[] allowedTeams = NeutralUnitTeam.toNeutralTeamArray(Arrays.copyOf(UnitTeam.ORDERED_TEAMS, level.playerCount));
            teamSelector = new UIEnumSelector<>(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI_BACK,
                    14 / 2f - UIEnumSelector.totalWidth(2f, 6.5f) / 2, -2.5f, 2f, 6.5f, allowedTeams, 1) {
                @Override
                public boolean posInside(ObjPos pos, InputType type) {
                    return true;
                }
            }.setDisplayTheme(editingTeam.uiColour).setOnChanged(() -> {
                editingTeam = teamSelector.getValue();
                teamSelector.setDisplayTheme(editingTeam.uiColour);
                unitDisplays.forEach(UITeamDisplay::updateImage);
                structureDisplays.forEach(UITeamDisplay::updateImage);
            });
        });

        unitDisplays.forEach(UITeamDisplay::updateImage);
        structureDisplays.forEach(UITeamDisplay::updateImage);

        new RenderElement(mainRenderer, RenderOrder.TILE_UNITS, g -> {
            for (int x = 0; x < level.tilesX; x++) {
                for (int y = 0; y < level.tilesY; y++) {
                    if (level.unitData[x][y].type() == null)
                        continue;
                    int finalY = y, finalX = x;
                    GameRenderer.renderOffset(level.tileSelector.getTile(x, y).renderPos, g, () -> {
                        level.unitData[finalX][finalY].render(g);
                    });
                }
            }
        });

        new LevelUIButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 9.5f, Renderable.top() - 2.5f, 8, 2, 1.4f, false, level, () -> saveMenu.setEnabled(true))
                .setText("Save").setBold();
        new LevelUIContainer<>(levelUIRenderer, level.buttonRegister, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU,
                0, 0, level).addRenderables((r, b) -> {
            saveMenu = new UISaveMenu<>(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, MainPanel.mapSaves, name -> new MapSave(level, name))
                    .setOnSave(() -> level.unsaved = false);
        });
        new OnButtonInput(level.buttonRegister, ButtonOrder.LEVEL_UI_BACK, t -> t.isCharInput && (t.c == 't' || t.c == '1'), () -> tabSwitcher.selectTab(0));
        new OnButtonInput(level.buttonRegister, ButtonOrder.LEVEL_UI_BACK, t -> t.isCharInput && (t.c == 'u' || t.c == '2'), () -> tabSwitcher.selectTab(1));
        new OnButtonInput(level.buttonRegister, ButtonOrder.LEVEL_UI_BACK, t -> t.isCharInput && (t.c == 's' || t.c == '3'), () -> tabSwitcher.selectTab(2));
        saveMenu.setEnabled(false);

        invalidContainer = new LevelUIContainer<>(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                0, 0, level);
        invalidContainer.addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new UIBox(10, 3).setColourTheme(UIColourTheme.DEEP_RED).translate(.5f, .5f),
                    new UITextLabel(10, 1, true).setTextLeftBold().updateTextLeft("Invalid map layout")
                            .setLabelColour(UIColourTheme.DEEP_RED).translate(0.2f, 4),
                    invalidText
            );
        });

        new LevelUIShapeButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                Renderable.right() / 2 + 5, Renderable.top() - 2.5f, 2, 2, false, level)
                .setShape(UIShapeButton::map).setOnClick(() -> {
                    mapUI.setEnabled(true);
                    mapUI.update();
                }).tooltip(t -> t.add(-1, UITooltip.dark(), "Open map"));
        mapUI = new LevelMapUI(levelUIRenderer, level.buttonRegister, level);
        mapUI.setEnabled(false);

        LevelUIContainer<LevelEditor> generateContainer = new LevelUIContainer<>(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                Renderable.right() / 2 - 4, Renderable.top() - 10.5f, level);
        generateContainer.addRenderables((r, b) -> {
            new UIClickBlockingBox(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI_BACK, 0, 0, 8, 7, box ->
                    box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER)).setZOrder(-2);
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new UITextLabel(6, 0.8f, false).setTextCenterBold()
                            .updateTextCenter("Enter seed:").translate(0.9f, 6),
                    new TextRenderer("Removes all units", 0.6f, TEXT_COLOUR)
                            .setItalic(true).setTextAlign(TextAlign.CENTER).translate(4, 1f)
            ).setZOrder(-1);
            UITextInputBox seedBox = new UITextInputBox(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                    0.5f, 4, 7, 1.5f, 0.8f, true, 15, InputType::isDigit);
            seedBox.setBold().setColourTheme(UIColourTheme.GREEN_SELECTED).tooltip(t -> t.add(12, UITooltip.dark(),
                    "Enter number as seed to generate map from, or leave blank for random seed. Seeds are not guaranteed to generate the same maps as other seed entry boxes."));
            new UIButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 1.5f, 2.2f, 5, 1.2f, 1f, false)
                    .setText("Generate").setBold().setOnClick(() -> {
                        long seed = seedBox.getText().isEmpty() ? new Random().nextLong() : Long.parseLong(seedBox.getText());
                        level.createRandom(seed);
                        level.clearUnits();
                        level.generateTiles();
                    }).setBoxCorner(0.35f);
        }).setEnabled(false);
        new LevelUIButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI_BACK,
                Renderable.right() / 2 - 4, Renderable.top() - 2.5f, 8, 2, 1.2f, true, level)
                .setBold().setColourTheme(UIColourTheme.GREEN_SELECTED).toggleMode().noDeselect().setText("Generate").setOnClick(() -> {
                    generateContainer.setEnabled(true);
                }).setOnDeselect(() -> {
                    generateContainer.setEnabled(false);
                }).tooltip(t -> t.add(-1, UITooltip.dark(), "Generate map from seed"));
    }

    public void setSaveName(String name) {
        saveMenu.saveFileNameBox.setText(name);
    }

    public void setInvalid(String message) {
        invalidText.updateText(message);
        invalidContainer.setEnabled(true);
    }

    public void setValid() {
        invalidContainer.setEnabled(false);
    }

    public TileType getEditingTileType() {
        return tabSwitcher.getSelectedTab() == 0 ? editingTileType : null;
    }

    public UnitType getEditingUnitType() {
        return (tabSwitcher.getSelectedTab() == 1 && editingTeam != NeutralUnitTeam.NEUTRAL) ? editingUnitType : null;
    }

    public StructureType getEditingStructureType() {
        return (tabSwitcher.getSelectedTab() == 2 && editingStructureType != null && (editingStructureType.hasNeutral || editingTeam != NeutralUnitTeam.NEUTRAL)) ? editingStructureType : null;
    }

    public boolean removeUnit() {
        return tabSwitcher.getSelectedTab() == 1 && removeUnit;
    }

    public boolean removeStructure() {
        return tabSwitcher.getSelectedTab() == 2 && removeStructure;
    }

    @Override
    protected boolean renderSelectedTile() {
        return false;
    }

    @Override
    public void delete() {
        super.delete();
        tileButtons.clear();
        unitButtons.clear();
        unitDisplays.clear();
        structureButtons.clear();
        structureDisplays.clear();
        tabSwitcher = null;
        saveMenu = null;
        teamSelector = null;
        confirm = null;
        mapUI = null;
    }

    private static class UITeamDisplay extends UIContainer {
        public ImageRenderer image;
        public Supplier<ImageRenderer> imageSupplier;

        public UITeamDisplay(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, Supplier<ImageRenderer> imageSupplier) {
            super(register, buttonRegister, order, buttonOrder, x, y);
            this.imageSupplier = imageSupplier;
        }

        public void updateImage() {
            image = imageSupplier.get();
        }

        @Override
        public void delete() {
            super.delete();
            imageSupplier = null;
            image = null;
        }
    }
}
