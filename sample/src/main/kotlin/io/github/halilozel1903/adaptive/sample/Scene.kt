package io.github.halilozel1903.adaptive.sample

/** Screenshot scenes, picked with the `scene` intent extra. */
enum class Scene(val key: String) {
    ListDetail("listdetail"),
    Supporting("supporting"),
    Compact("compact"),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}
