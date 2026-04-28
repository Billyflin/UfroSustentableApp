package com.ecosense

import com.ecosense.model.Grupo
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.GrupoService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RF14PuntosGrupalesSpec : BehaviorSpec({

    Given("un usuario miembro de un grupo que realiza una acción de reciclaje") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 100))
        val usuario = Usuario("U001", "ana@ufro.cl", "Ana", puntos = 50, grupoId = "G001")

        When("el sistema le asigna 30 puntos por la acción") {
            service.agregarPuntosAlGrupo(usuario, 30)

            Then("los puntos personales del usuario aumentan en 30") {
                usuario.puntos shouldBe 80
            }

            Then("el puntaje total del grupo también aumenta en 30") {
                service.obtenerGrupo("G001")!!.puntajeTotal shouldBe 130
            }
        }
    }

    Given("un usuario miembro que realiza múltiples acciones de reciclaje") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 0))
        val usuario = Usuario("U001", "ana@ufro.cl", "Ana", puntos = 0, grupoId = "G001")

        When("se registran tres acciones consecutivas de 10, 20 y 30 puntos") {
            service.agregarPuntosAlGrupo(usuario, 10)
            service.agregarPuntosAlGrupo(usuario, 20)
            service.agregarPuntosAlGrupo(usuario, 30)

            Then("el puntaje grupal acumula la suma de las tres acciones") {
                service.obtenerGrupo("G001")!!.puntajeTotal shouldBe 60
            }
        }
    }

    Given("un usuario sin grupo que realiza una acción de reciclaje") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 100))
        val usuario = Usuario("U001", "ana@ufro.cl", "Ana", puntos = 0, grupoId = null)

        When("el sistema le asigna 40 puntos") {
            service.agregarPuntosAlGrupo(usuario, 40)

            Then("los puntos se acumulan únicamente en la cuenta personal del usuario") {
                usuario.puntos shouldBe 40
            }

            Then("el puntaje de cualquier grupo existente no se modifica") {
                service.obtenerGrupo("G001")!!.puntajeTotal shouldBe 100
            }
        }
    }

    Given("un usuario miembro al que se asignan exactamente 0 puntos") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 200))
        val usuario = Usuario("U001", "ana@ufro.cl", "Ana", puntos = 10, grupoId = "G001")

        When("se registran 0 puntos por la acción") {
            service.agregarPuntosAlGrupo(usuario, 0)

            Then("el puntaje grupal no cambia") {
                service.obtenerGrupo("G001")!!.puntajeTotal shouldBe 200
            }
        }
    }
})


