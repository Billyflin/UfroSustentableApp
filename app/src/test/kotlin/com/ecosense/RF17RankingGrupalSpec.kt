package com.ecosense

import com.ecosense.model.Grupo
import com.ecosense.model.TipoGrupo
import com.ecosense.service.RankingService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class RF17RankingGrupalSpec : BehaviorSpec({

    Given("tres grupos con distintos puntajes totales") {
        val service = RankingService()
        val grupos = listOf(
            Grupo("G1", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 500),
            Grupo("G2", "RecicladoresUFRO", tipo = TipoGrupo.PUBLICO, puntajeTotal = 800),
            Grupo("G3", "GreenTeam", tipo = TipoGrupo.PUBLICO, puntajeTotal = 200)
        )

        When("se calcula el ranking de grupos") {
            val ranking = service.obtenerRankingGrupal(grupos)

            Then("el grupo con mayor puntaje aparece en la primera posición") {
                ranking[0].grupo.id shouldBe "G2"
                ranking[0].posicion shouldBe 1
            }

            Then("los grupos están ordenados correctamente de mayor a menor puntaje") {
                ranking[1].grupo.id shouldBe "G1"
                ranking[2].grupo.id shouldBe "G3"
            }
        }

        When("un usuario consulta la posición de su grupo en el ranking") {
            val posicion = service.obtenerPosicionGrupo("G1", grupos)

            Then("se muestra la posición correcta del grupo") {
                posicion shouldBe 2
            }
        }
    }

    Given("ningún grupo registrado en el sistema") {
        val service = RankingService()

        When("se solicita el ranking grupal") {
            val ranking = service.obtenerRankingGrupal(emptyList())

            Then("el ranking devuelto está vacío") {
                ranking.shouldBeEmpty()
            }
        }
    }

    Given("un grupo con 0 puntos compitiendo con grupos que sí tienen puntos") {
        val service = RankingService()
        val grupos = listOf(
            Grupo("G1", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 300),
            Grupo("G2", "SinPuntos", tipo = TipoGrupo.PUBLICO, puntajeTotal = 0)
        )

        When("se calcula el ranking") {
            val ranking = service.obtenerRankingGrupal(grupos)

            Then("el grupo con 0 puntos aparece al final del ranking") {
                ranking.last().grupo.id shouldBe "G2"
            }
        }
    }

    Given("un usuario que no pertenece a ningún grupo") {
        val service = RankingService()
        val grupos = listOf(Grupo("G1", "EcoVerde", tipo = TipoGrupo.PUBLICO, puntajeTotal = 300))

        When("busca la posición de un grupo inexistente en el ranking") {
            val posicion = service.obtenerPosicionGrupo("G999", grupos)

            Then("el sistema retorna null indicando que el grupo no existe") {
                posicion.shouldBeNull()
            }
        }
    }
})

