package com.ecosense

import com.ecosense.model.Usuario
import com.ecosense.service.RankingService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class RF10RankingGlobalSpec : BehaviorSpec({

    Given("usuarios con distintos puntajes registrados en el sistema") {
        val service = RankingService()
        val usuarios = listOf(
            Usuario("U1", "ana@ufro.cl", "Ana", puntos = 200),
            Usuario("U2", "bob@ufro.cl", "Bob", puntos = 50),
            Usuario("U3", "carlos@ufro.cl", "Carlos", puntos = 150)
        )

        When("se solicita el ranking global") {
            val ranking = service.obtenerRankingGlobal(usuarios)

            Then("los usuarios aparecen ordenados de mayor a menor puntaje") {
                ranking[0].usuario.id shouldBe "U1"
                ranking[1].usuario.id shouldBe "U3"
                ranking[2].usuario.id shouldBe "U2"
            }

            Then("las posiciones son correlativas comenzando en 1") {
                ranking[0].posicion shouldBe 1
                ranking[1].posicion shouldBe 2
                ranking[2].posicion shouldBe 3
            }
        }

        When("se consulta la posición de un usuario específico") {
            val posicion = service.obtenerPosicionGlobal("U3", usuarios)

            Then("se retorna su posición correcta en el ranking") {
                posicion shouldBe 2
            }
        }
    }

    Given("un usuario sin puntos compitiendo con otro que sí tiene puntos") {
        val service = RankingService()
        val usuarios = listOf(
            Usuario("U1", "ana@ufro.cl", "Ana", puntos = 100),
            Usuario("U2", "bob@ufro.cl", "Bob", puntos = 0)
        )

        When("se calcula el ranking") {
            val ranking = service.obtenerRankingGlobal(usuarios)

            Then("el usuario sin puntos aparece al final del ranking") {
                ranking.last().usuario.id shouldBe "U2"
                ranking.last().posicion shouldBe 2
            }
        }
    }

    Given("ningún usuario registrado en el sistema") {
        val service = RankingService()

        When("se solicita el ranking global") {
            val ranking = service.obtenerRankingGlobal(emptyList())

            Then("el ranking devuelto está vacío") {
                ranking.shouldBeEmpty()
            }
        }
    }

    Given("dos usuarios con exactamente el mismo puntaje") {
        val service = RankingService()
        val usuarios = listOf(
            Usuario("U1", "ana@ufro.cl", "Ana", puntos = 100),
            Usuario("U2", "bob@ufro.cl", "Bob", puntos = 100)
        )

        When("se calcula el ranking") {
            val ranking = service.obtenerRankingGlobal(usuarios)

            Then("ambos usuarios aparecen en el ranking con posiciones distintas") {
                ranking shouldHaveSize 2
                ranking[0].posicion shouldBe 1
                ranking[1].posicion shouldBe 2
            }
        }
    }
})


