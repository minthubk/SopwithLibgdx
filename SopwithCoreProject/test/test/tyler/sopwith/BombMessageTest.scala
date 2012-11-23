package test.tyler.sopwith

import org.junit.Assert._
import org.junit.Test

import net.tyler.math.CartesianVector2f
import net.tyler.math.PolarConstants
import net.tyler.messaging.MessagePassing
import net.tyler.messaging.MessagingComponent
import net.tyler.sopwith.BombDestroyed
import net.tyler.sopwith.BombReleased
import net.tyler.sopwith.BombState
import net.tyler.sopwith.BuildingDestroyed
import net.tyler.sopwith.Configuration
import net.tyler.sopwith.InGameStateQuerier
import net.tyler.sopwith.PlaneState
import net.tyler.sopwith.PlaneVelocityChange

/**
 * Test that queries regarding the bombs return the correct set of information.
 */
class BombMessageTest {
  
  private val FP_DELTA = 0.01
  
  val initialPlaneState = new PlaneState(PolarConstants.zero, 
                                         new CartesianVector2f(1f, 2f).toPolar, 
                                         new CartesianVector2f(10f, 10f).toPolar, 
                                         false)
  
  trait StateTester {
    private val inGameMessageTypes = List(classOf[PlaneVelocityChange],
                                          classOf[BombDestroyed],
                                          classOf[BombReleased],
                                          classOf[BuildingDestroyed])
    val messagePassing = new MessagePassing
    val messagingComponent = new MessagingComponent(messagePassing, inGameMessageTypes)
    
    val querier = new InGameStateQuerier(initialPlaneState, List(), 5, 0, messagingComponent)
  }
  
  @Test def oneLiveBomb {
    new ApplicationTester with StateTester {
      messagePassing.send(new BombReleased(new CartesianVector2f(10f, 100f), 10))
      
      assertEquals(querier.liveBombs(9).size, 0)
      assertEquals(querier.liveBombs(11).size, 1)
      
      val msg: BombState = querier.liveBombs(1010).head
      
      assertEquals(msg.velocity.x, 0f, FP_DELTA)
      assertEquals(msg.velocity.y, Configuration.BOMB_ACCELERATION, FP_DELTA)
      assertEquals(msg.position.x, 10f, FP_DELTA)
      assertEquals(msg.position.y, 100f + 1.5f * Configuration.BOMB_ACCELERATION, FP_DELTA)
    }
  }
  
  @Test def bombsRemaining {
    new ApplicationTester with StateTester {
      messagePassing.send(new BombReleased(new CartesianVector2f(10f, 100f), 10))
      messagePassing.send(new BombReleased(new CartesianVector2f(10f, 100f), 15))
      messagePassing.send(new BombReleased(new CartesianVector2f(10f, 100f), 20))
      
      assertEquals(querier.bombsRemaining(5), 5)
      assertEquals(querier.bombsRemaining(11), 4)
      assertEquals(querier.bombsRemaining(16), 3)
      assertEquals(querier.bombsRemaining(21), 2)
    }
  }
}