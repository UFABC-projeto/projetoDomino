package prof.ronny.appdominoai

// Código Kotlin para capturar imagens da câmera em tempo real e processá-las com OpenCV

// Importe as bibliotecas necessárias

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.BuildConfig
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


import org.opencv.core.CvType
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect

import org.opencv.core.Scalar
import org.w3c.dom.Text

import java.util.ArrayList


// Inicialize o OpenCV


// Implemente a interface CvCameraViewListener2 para receber os quadros da câmera
class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    private lateinit var cameraView: CameraBridgeViewBase
    val MY_PERMISSIONS_REQUEST_CAMERA = 0
    lateinit var tvDominosDetected:TextView
    val pecasDominos: MutableList<PecaDomino> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvDominosDetected= findViewById(R.id.tvDominosDetected)

        if(OpenCVLoader.initDebug())
        {
            Log.e("OpenCVLoader", "onCreate: Funcionou INIT")
        }
        else{
            Log.e("OpenCVLoader", "onCreate: Falhou INIT")
        }

        // Inicialize a visualização da câmera
        cameraView = findViewById(R.id.camera_view)
        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.setCameraPermissionGranted()
        cameraView.setCameraIndex(0)



        cameraView.enableView()

    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i("ok", "onCameraViewStarted: ")
    }

    override fun onCameraViewStopped() {
        Log.e("erro", "onCameraViewStopped: ")
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val frame: Mat = inputFrame.gray() // Convertendo para escala de cinza

        // Pré-processamento
        Imgproc.GaussianBlur(frame, frame, Size(5.0, 5.0), 0.0)
        Imgproc.threshold(frame, frame, 120.0, 255.0, Imgproc.THRESH_BINARY)

        // Detecção de bordas
        val edges = Mat()
        Imgproc.Canny(frame, edges, 80.0, 180.0)

        // Encontrando contornos
        val contours: List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        // Filtrar contornos e desenhar
        val result = inputFrame.rgba()
        contours.forEach { contour ->
            val contourArea = Imgproc.contourArea(contour)
            if (contourArea > MIN_CONTOUR_AREA) {
                // Encaixar um retângulo no contorno e verificar a razão de aspecto
                val rect = Imgproc.boundingRect(contour)
                val aspectRatio = rect.width.toDouble() / rect.height.toDouble()
                pecasDominos.forEach { peca ->
                    Log.i("PecaDominoLista", "Peça: $peca")
                }

                // Verificar se o contorno tem uma forma retangular
                if (aspectRatio > 0.4 && aspectRatio < 0.8 ||aspectRatio > 1.8 && aspectRatio < 2.2 ) {

                    val pecaMat = inputFrame.rgba().submat(rect) // Extrai a peça de dominó da imagem principal

                    val pecaDomino = processarImagem(pecaMat)


                    if (pecaDomino != null && !contemPeca(pecasDominos, pecaDomino)) {
                            pecasDominos.add(pecaDomino)
                            Log.i("PecaDominoDetected", "Peça detectada: $pecaDomino")


                        val dominosString = pecasDominos.joinToString(separator = " , ") { peca ->
                            "[${peca.numberTop},${peca.numberBottom}]"
                        }
                        runOnUiThread {
                            tvDominosDetected.text = "Peças de Dominó Detectadas: $dominosString"
                        }
                    }

                    // Desenhar um retângulo ao redor do contorno
                    Imgproc.rectangle(result, rect.tl(), rect.br(), Scalar(0.0, 255.0, 0.0), 2)

                }
            }
        }

        return result
    }

    companion object {
        const val MIN_CONTOUR_AREA = 5000
    }

    fun processarImagem(pecaMat: Mat): PecaDomino? {
        // Supondo que 'pecaMat' é a imagem da peça de dominó a ser processada.
        // Primeiro, determinamos o retângulo delimitador para 'pecaMat'.
        // Isso pode ser feito externamente e passado para a função, ou pode ser calculado aqui.

        // Por exemplo:
        val rect = Rect(0, 0, pecaMat.width(), pecaMat.height())

        // Crie a instância de PecaDomino com o retângulo.
        val pecaDomino = PecaDomino(rect)

        // Aqui você adicionaria a lógica para processar 'pecaMat' e detectar os números.
        // Este é um exemplo e deve ser adaptado com sua lógica específica de detecção de números.
        val numberTop = detectarNumeroNaPeca(pecaMat, isTop = true)  // Implemente esta função.
        val numberBottom = detectarNumeroNaPeca(pecaMat, isTop = false)  // Implemente esta função.

        pecaDomino.setNumbers(numberTop, numberBottom)

        if(numberBottom>6 || numberBottom > 6 )
        {
            return null
        }

        return pecaDomino
    }

    fun contemPeca(lista: List<PecaDomino>, peca: PecaDomino): Boolean {
        return lista.any { it.isEquivalentTo(peca) }
    }

    fun detectarNumeroNaPeca(pecaMat: Mat, isTop: Boolean): Int {
        // Verificar a orientação da peça (vertical ou horizontal)
        val isVertical = pecaMat.height() > pecaMat.width()

        // Definir a região de interesse (ROI) baseada na orientação
        val roi: Rect = if (isVertical) {
            if (isTop) Rect(0, 0, pecaMat.width(), pecaMat.height() / 2) // Metade superior
            else Rect(0, pecaMat.height() / 2, pecaMat.width(), pecaMat.height() / 2) // Metade inferior
        } else {
            if (isTop) Rect(0, 0, pecaMat.width() / 2, pecaMat.height()) // Metade esquerda
            else Rect(pecaMat.width() / 2, 0, pecaMat.width() / 2, pecaMat.height()) // Metade direita
        }

        // Extrair a ROI da imagem
        val roiMat = Mat(pecaMat, roi)

        // Processar a ROI para detectar círculos (pontos)
        val processedMat = Mat()
        Imgproc.cvtColor(roiMat, processedMat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(processedMat, processedMat, Size(9.0, 9.0), 2.0)
        Imgproc.threshold(processedMat, processedMat, 60.0, 255.0, Imgproc.THRESH_BINARY_INV)

        // Encontrar contornos
        val contours: List<MatOfPoint> = ArrayList()
        Imgproc.findContours(processedMat, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Contar os contornos que se assemelham a círculos
        var count = 0
        for (contour in contours) {
            val matOfPoint2f = MatOfPoint2f(*contour.toArray())
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(matOfPoint2f, approx, 0.02 * Imgproc.arcLength(matOfPoint2f, true), true)
            val area = Imgproc.contourArea(contour)

            // Critérios para ser considerado um ponto: forma circular e tamanho adequado

            val MIN_AREA=50
            if (area > MIN_AREA && approx.total() > 6) {
                count++
            }
        }

        // Limpar
        roiMat.release()
        processedMat.release()

        return count
    }



}