package render;

public enum RenderOrder {
    //Higher up on this list means lower Z-order

    NONE,

    //In-game level
    BACKGROUND,
    TERRAIN,
    TILE_BORDER,
    TILE_HIGHLIGHT,
    ACTION_BELOW_UNITS,
    TILE_UNITS,
    FOG_OF_WAR,
    TILE_BORDER_HIGHLIGHTS,
    CAPTURE_BAR,
    ACTION_ABOVE_UNITS,
    ACTION_SELECTOR,
    DAMAGE_UI,
    UI,

    //Level menus / UI
    LEVEL_UI,
    UNIT_INFO_SCREEN_BACKGROUND,
    UNIT_INFO_SCREEN,

    //Main menu screen
    TITLE_SCREEN_BACKGROUND,
    TITLE_SCREEN_BUTTON_BACKGROUND,
    TITLE_SCREEN_BUTTONS
}
