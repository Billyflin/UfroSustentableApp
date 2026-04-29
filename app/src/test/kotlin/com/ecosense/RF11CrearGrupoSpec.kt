package com.ecosense

import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.service.GrupoService
import com.ecosense.service.ResultadoCreacion
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RF11CrearGrupoSpec : BehaviorSpec({

    Given("un usuario autenticado que quiere crear un grupo") {
        val creadorId = "U001"

        When("ingresa nombre y descripción válidos para un grupo público") {
            val service = GrupoService()
            val resultado = service.crearGrupo(creadorId, "EcoVerde", "Grupo de reciclaje")

            Then("el grupo se crea exitosamente") {
                resultado.shouldBeInstanceOf<ResultadoCreacion.Exitoso>()
            }

            Then("el creador queda registrado como administrador del grupo") {
                val grupo = (resultado as ResultadoCreacion.Exitoso).grupo
                grupo.miembros.first().usuarioId shouldBe creadorId
                grupo.miembros.first().rol shouldBe RolMiembro.ADMINISTRADOR
            }
        }

        When("crea un grupo de tipo privado") {
            val service = GrupoService()
            val resultado = service.crearGrupo(creadorId, "EcoPrivado", "Grupo privado", TipoGrupo.PRIVADO)

            Then("el grupo se crea con tipo PRIVADO") {
                val grupo = (resultado as ResultadoCreacion.Exitoso).grupo
                grupo.tipo shouldBe TipoGrupo.PRIVADO
            }
        }

        When("intenta crear un grupo con nombre vacío") {
            val service = GrupoService()
            val resultado = service.crearGrupo(creadorId, "", "Sin nombre")

            Then("el sistema retorna un error indicando que el nombre es obligatorio") {
                val error = resultado.shouldBeInstanceOf<ResultadoCreacion.Error>()
                error.mensaje shouldBe "El nombre del grupo no puede estar vacío"
            }
        }

        When("intenta crear un grupo con nombre compuesto solo de espacios") {
            val service = GrupoService()
            val resultado = service.crearGrupo(creadorId, "   ", "Solo espacios")

            Then("el sistema retorna un error por nombre inválido") {
                resultado.shouldBeInstanceOf<ResultadoCreacion.Error>()
            }
        }

        When("crea un grupo con nombre de exactamente un carácter") {
            val service = GrupoService()
            val resultado = service.crearGrupo(creadorId, "E", "Grupo mínimo")

            Then("el grupo se crea exitosamente con ese nombre") {
                val exitoso = resultado.shouldBeInstanceOf<ResultadoCreacion.Exitoso>()
                exitoso.grupo.nombre shouldBe "E"
            }
        }
    }
})
