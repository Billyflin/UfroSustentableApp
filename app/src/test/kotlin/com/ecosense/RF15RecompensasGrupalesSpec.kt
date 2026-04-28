package com.ecosense

import com.ecosense.model.Grupo
import com.ecosense.model.TipoGrupo
import com.ecosense.service.GrupoService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RF15RecompensasGrupalesSpec : BehaviorSpec({

    Given("un grupo que supera la meta de puntaje establecida") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 1200, metaPuntaje = 1000))

        When("el sistema verifica la recompensa grupal") {
            val recompensa = service.verificarRecompensaGrupal("G001")

            Then("la recompensa queda disponible para los integrantes") {
                recompensa.shouldNotBeNull()
                recompensa.grupoId shouldBe "G001"
            }
        }
    }

    Given("un grupo que no ha alcanzado la meta de puntaje") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 600, metaPuntaje = 1000))

        When("se verifica si hay recompensa disponible") {
            val recompensa = service.verificarRecompensaGrupal("G001")

            Then("no hay recompensa disponible") {
                recompensa.shouldBeNull()
            }
        }

        When("se consulta el progreso hacia la meta") {
            val progreso = service.progresoHaciaRecompensa("G001")

            Then("el sistema indica cuántos puntos faltan para alcanzar la meta") {
                progreso!!.puntajeRestante shouldBe 400
                progreso.puntajeActual shouldBe 600
                progreso.metaPuntaje shouldBe 1000
            }
        }
    }

    Given("un identificador de grupo que no existe en el sistema") {
        val service = GrupoService()

        When("se verifica la recompensa para ese grupo") {
            val recompensa = service.verificarRecompensaGrupal("G999")

            Then("el sistema no genera ninguna recompensa") {
                recompensa.shouldBeNull()
            }
        }
    }

    Given("un grupo con exactamente el puntaje de la meta") {
        val service = GrupoService()
        service.agregarGrupo(Grupo("G001", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 1000, metaPuntaje = 1000))

        When("se verifica la recompensa grupal") {
            val recompensa = service.verificarRecompensaGrupal("G001")

            Then("la recompensa se habilita al alcanzar exactamente la meta") {
                recompensa.shouldNotBeNull()
            }
        }
    }
})

