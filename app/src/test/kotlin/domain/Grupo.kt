package domain

enum class TipoGrupo { PUBLICO, PRIVADO }

data class Grupo(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val tipo: TipoGrupo,
    val miembros: MutableList<String> = mutableListOf(),
    var puntajeTotal: Int = 0
)
