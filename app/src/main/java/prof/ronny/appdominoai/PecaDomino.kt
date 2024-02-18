package prof.ronny.appdominoai


import android.graphics.Point
import org.opencv.core.Rect

class PecaDomino {
    var rect: Rect        // Retângulo delimitador da peça
    var isVertical: Boolean  // Orientação da peça
    var numberTop: Int?      // Número na parte superior
    var numberBottom: Int?   // Número na parte inferior

    constructor(rect: Rect) {
        this.rect = rect
        this.isVertical = rect.height > rect.width
        this.numberTop = null
        this.numberBottom = null
    }

    fun setNumbers(top: Int, bottom: Int) {
        this.numberTop = top
        this.numberBottom = bottom
    }

    fun getCenter(): Point {
        return Point(rect.x + rect.width / 2, rect.y + rect.height / 2)
    }

    fun isEquivalentTo(other: PecaDomino): Boolean {
        val numbers = setOf(this.numberTop, this.numberBottom)
        val otherNumbers = setOf(other.numberTop, other.numberBottom)
        return numbers == otherNumbers
    }

    override fun toString(): String {
        return "DominoPiece(rect=$rect, isVertical=$isVertical, numberTop=$numberTop, numberBottom=$numberBottom)"
    }
}
