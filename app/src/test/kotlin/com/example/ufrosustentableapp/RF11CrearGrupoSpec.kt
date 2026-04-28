package com.example.ufrosustentableapp

import com.example.ufrosustentableapp.model.RolMiembro
import com.example.ufrosustentableapp.model.TipoGrupo
import com.example.ufrosustentableapp.service.GrupoService
import com.example.ufrosustentableapp.service.ResultadoCreacion
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
                resultado.shouldBeInstanceOf<ResultadoCreacion.Error>()
                (resultado as ResultadoCreacion.Error).mensaje shouldBe "El nombre del grupo no puede estar vacío"
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
                resultado.shouldBeInstanceOf<ResultadoCreacion.Exitoso>()
                (resultado as ResultadoCreacion.Exitoso).grupo.nombre shouldBe "E"
            }
        }
    }
})
