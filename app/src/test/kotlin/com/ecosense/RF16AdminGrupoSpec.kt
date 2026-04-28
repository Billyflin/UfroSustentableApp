package com.ecosense

import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.service.AccionGestion
import com.ecosense.service.GrupoService
import com.ecosense.service.ResultadoGestion
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RF16AdminGrupoSpec : BehaviorSpec({

    fun setupGrupoConAdmin(): GrupoService {
        val service = GrupoService()
        service.agregarGrupo(
            Grupo(
                id = "G001", nombre = "EcoVerde", tipo = TipoGrupo.PUBLICO,
                miembros = mutableListOf(
                    MiembroGrupo("ADMIN", RolMiembro.ADMINISTRADOR),
                    MiembroGrupo("U001", RolMiembro.MIEMBRO)
                )
            )
        )
        return service
    }

    Given("un administrador autenticado gestionando su grupo") {

        When("agrega un nuevo usuario al grupo") {
            val service = setupGrupoConAdmin()
            val resultado = service.gestionarMiembro("ADMIN", "G001", AccionGestion.AGREGAR, "U002")

            Then("la operación es exitosa") {
                resultado shouldBe ResultadoGestion.Exitoso
            }

            Then("el nuevo usuario aparece en la lista de miembros") {
                service.obtenerGrupo("G001")!!.miembros.any { it.usuarioId == "U002" } shouldBe true
            }
        }

        When("elimina a un miembro existente del grupo") {
            val service = setupGrupoConAdmin()
            val resultado = service.gestionarMiembro("ADMIN", "G001", AccionGestion.ELIMINAR, "U001")

            Then("la operación es exitosa") {
                resultado shouldBe ResultadoGestion.Exitoso
            }

            Then("el miembro eliminado ya no aparece en el grupo") {
                service.obtenerGrupo("G001")!!.miembros.none { it.usuarioId == "U001" } shouldBe true
            }
        }
    }

    Given("un usuario sin rol de administrador que intenta gestionar el grupo") {

        When("intenta agregar un nuevo miembro") {
            val service = setupGrupoConAdmin()
            val resultado = service.gestionarMiembro("U001", "G001", AccionGestion.AGREGAR, "U003")

            Then("el sistema bloquea la acción por falta de permisos") {
                resultado.shouldBeInstanceOf<ResultadoGestion.Error>()
                (resultado as ResultadoGestion.Error).mensaje shouldBe "No tienes permisos de administrador"
            }
        }
    }

    Given("un administrador que intenta agregar un miembro que ya pertenece al grupo") {

        When("intenta agregar nuevamente a U001 que ya es miembro") {
            val service = setupGrupoConAdmin()
            val resultado = service.gestionarMiembro("ADMIN", "G001", AccionGestion.AGREGAR, "U001")

            Then("el sistema retorna error indicando que ya es miembro") {
                resultado.shouldBeInstanceOf<ResultadoGestion.Error>()
                (resultado as ResultadoGestion.Error).mensaje shouldBe "El usuario ya es miembro del grupo"
            }
        }
    }

    Given("un administrador que intenta eliminar un usuario que no existe en el grupo") {

        When("intenta eliminar a U999 que no es miembro") {
            val service = setupGrupoConAdmin()
            val resultado = service.gestionarMiembro("ADMIN", "G001", AccionGestion.ELIMINAR, "U999")

            Then("el sistema retorna error indicando que el usuario no es miembro") {
                resultado.shouldBeInstanceOf<ResultadoGestion.Error>()
                (resultado as ResultadoGestion.Error).mensaje shouldBe "El usuario no es miembro del grupo"
            }
        }
    }
})


