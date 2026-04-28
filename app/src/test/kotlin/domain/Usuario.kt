package domain

data class Usuario(
    val id: String,
    val email: String,
    val nombre: String,
    val puntos: Int,
    var grupoId: String? = null
)
