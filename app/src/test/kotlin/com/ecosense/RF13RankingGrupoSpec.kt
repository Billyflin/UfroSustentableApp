package com.ecosense

import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.RankingService
import com.ecosense.service.ResultadoRankingGrupo
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RF13RankingGrupoSpec : BehaviorSpec({

    // Ranking global: U1(300) > U4(250) > U3(200) > U2(100)
    // Posiciones globales: U1=1, U4=2, U3=3, U2=4
    // Miembros del grupo: U1, U2, U3 (U4 no pertenece)
    // Ranking interno:    U1(300)=pos1, U3(200)=pos2, U2(100)=pos3

    Given("un grupo con tres miembros y usuarios externos con distintos puntajes") {
        val service = RankingService()
        val grupo = Grupo(
            id = "G001", nombre = "EcoVerde", tipo = TipoGrupo.PUBLICO,
            miembros = mutableListOf(MiembroGrupo("U1"), MiembroGrupo("U2"), MiembroGrupo("U3"))
        )
        val todosLosUsuarios = listOf(
            Usuario("U1", "ana@ufro.cl", "Ana", puntos = 300),
            Usuario("U2", "bob@ufro.cl", "Bob", puntos = 100),
            Usuario("U3", "carlos@ufro.cl", "Carlos", puntos = 200),
            Usuario("U4", "diana@ufro.cl", "Diana", puntos = 250)
        )

        When("se solicita el ranking interno del grupo") {
            val resultado = service.obtenerRankingInterno(grupo, todosLosUsuarios)

            Then("los miembros están ordenados por puntaje de mayor a menor") {
                val ranking = (resultado as ResultadoRankingGrupo.Exitoso).ranking
                ranking[0].usuario.id shouldBe "U1"
                ranking[1].usuario.id shouldBe "U3"
                ranking[2].usuario.id shouldBe "U2"
            }

            Then("cada miembro tiene su posición global correctamente asignada") {
                val ranking = (resultado as ResultadoRankingGrupo.Exitoso).ranking
                ranking.find { it.usuario.id == "U1" }!!.posicionGlobal shouldBe 1
                ranking.find { it.usuario.id == "U3" }!!.posicionGlobal shouldBe 3
                ranking.find { it.usuario.id == "U2" }!!.posicionGlobal shouldBe 4
            }
        }
    }

    Given("un usuario que intenta ver el ranking de un grupo sin miembros") {
        val service = RankingService()
        val grupoVacio = Grupo(id = "G002", nombre = "Vacío", tipo = TipoGrupo.PUBLICO)

        When("se solicita el ranking interno del grupo vacío") {
            val resultado = service.obtenerRankingInterno(grupoVacio, emptyList())

            Then("el sistema retorna error indicando que no hay miembros") {
                resultado.shouldBeInstanceOf<ResultadoRankingGrupo.Error>()
            }
        }
    }

    Given("un grupo con exactamente un miembro") {
        val service = RankingService()
        val grupo = Grupo(
            id = "G003", nombre = "Solo", tipo = TipoGrupo.PUBLICO,
            miembros = mutableListOf(MiembroGrupo("U1"))
        )
        val usuarios = listOf(Usuario("U1", "solo@ufro.cl", "Solo", puntos = 50))

        When("se solicita el ranking interno") {
            val resultado = service.obtenerRankingInterno(grupo, usuarios)

            Then("el único miembro aparece en la posición 1 con posición global 1") {
                val ranking = (resultado as ResultadoRankingGrupo.Exitoso).ranking
                ranking[0].posicion shouldBe 1
                ranking[0].posicionGlobal shouldBe 1
            }
        }
    }
})


