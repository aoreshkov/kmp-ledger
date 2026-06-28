package app.oreshkov.ledger.core.model.settings

/**
 * User-selectable theme preference. [SYSTEM] follows the OS dark/light setting;
 * a plain boolean can't express "follow the system", so this is modelled as an enum.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}
