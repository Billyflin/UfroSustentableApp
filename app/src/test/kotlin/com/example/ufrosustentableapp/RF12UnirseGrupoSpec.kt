package com.example.ufrosustentableapp

import com.example.ufrosustentableapp.model.Grupo
import com.example.ufrosustentableapp.model.TipoGrupo
import com.example.ufrosustentableapp.model.Usuario
import com.example.ufrosustentableapp.service.GrupoService
import com.example.ufrosustentableapp.service.ResultadoUnion
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RF12UnirseGrupoSpec : BehaviorSpec({

    fun setupService(): GrupoService {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 100))
        service.agregarGrupo(Grupo("G002", "RecicladoresUFRO", tipo = TipoGrupo.PRIVADO, puntajeTotal = 50))
        return service
    }

    Given("grupos disponibles en el sistema") {

        When("un usuario sin grupo intenta unirse a un grupo público") {
            val service = setupService()
            val usuario = Usuario("U001", "ana@ufro.cl", "Ana", puntos = 20)
            val resultado = service.unirseAGrupo(usuario, "G001")

            Then("el resultado es Exitoso") {
                resultado.shouldBeInstanceOf<ResultadoUnion.Exitoso>()
            }

            Then("el usuario queda asignado al grupo") {
                usuario.grupoId shouldBe "G001"
            }
        }

        When("un usuario con 30 puntos se une al grupo G001 que tiene 100 puntos") {
            val service = setupService()
            val usuario = Usuario("U002", "carlos@ufro.cl", "Carlos", puntos = 30)
            service.unirseAGrupo(usuario, "G001")

            Then("el puntaje total del grupo aumenta a 130") {
                service.obtenerGrupo("G001")!!.puntajeTotal shouldBe 130
            }
        }

        When("un usuario sin grupo intenta unirse a un grupo privado") {
            val service = setupService()
            val usuario = Usuario("U003", "lucia@ufro.cl", "Lucia", puntos = 15)
            val resultado = service.unirseAGrupo(usuario, "G002")

            Then("el resultado es Pendiente de aprobación") {
                resultado.shouldBeInstanceOf<ResultadoUnion.Pendiente>()
            }

            Then("el usuario no queda asignado a ningún grupo") {
                usuario.grupoId.shouldBeNull()
            }
        }

        When("un usuario que ya pertenece a un grupo intenta unirse a otro") {
            val service = setupService()
            val usuario = Usuario("U004", "pedro@ufro.cl", "Pedro", puntos = 10, grupoId = "G002")
            val resultado = service.unirseAGrupo(usuario, "G001")

            Then("el resultado es Error indicando que ya pertenece a un grupo") {
                resultado.shouldBeInstanceOf<ResultadoUnion.Error>()
                (resultado as ResultadoUnion.Error).mensaje shouldBe "El usuario ya pertenece a un grupo"
            }
        }

        When("un usuario intenta unirse al mismo grupo al que ya pertenece") {
            val service = setupService()
            val usuario = Usuario("U005", "maria@ufro.cl", "Maria", puntos = 25, grupoId = "G001")
            val resultado = service.unirseAGrupo(usuario, "G001")

            Then("el resultado es Error indicando que ya es miembro de ese grupo") {
                resultado.shouldBeInstanceOf<ResultadoUnion.Error>()
                (resultado as ResultadoUnion.Error).mensaje shouldBe "El usuario ya pertenece a este grupo"
            }
        }
    }
})
