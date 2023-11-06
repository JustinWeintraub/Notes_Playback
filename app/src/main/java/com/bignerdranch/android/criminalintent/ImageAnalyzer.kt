package com.bignerdranch.android.criminalintent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.FileProvider
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.Triangle
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.AccessController.getContext


private const val TAG = "Face Detection"

fun getRotatedBitmap(photoFile: File): Bitmap {
    val exif = ExifInterface(photoFile.absolutePath)
    val rotationInDegrees = when (exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    val matrix = Matrix().apply {
        postRotate(rotationInDegrees.toFloat())
    }

    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun rotateImage(context: Context, imageUri: Uri, angle: Float) {
    val input: InputStream = context.contentResolver.openInputStream(imageUri)!!
    val bitmap: Bitmap = BitmapFactory.decodeStream(input)
    val matrix = Matrix()
    matrix.postRotate(angle)

    val rotatedBitmap: Bitmap = Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
    //  Write the bitmap to where the URI points
    try {
        context.contentResolver.openOutputStream(imageUri).also {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
            it?.close()
        }
    } catch (e: IOException) {
        Log.v(TAG, "Failed to save uri image")
    }

    input.close()

}

fun detectFaces(context: Context, fileName: String, imageView: ImageView, bitmap: Bitmap, callback: (Int) -> Unit) {

    // High-accuracy landmark detection and face classification
    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(highAccuracyOpts)

    val background: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(background)
    val paint = Paint()
    paint.strokeWidth = 36f
    paint.color = Color.RED
    paint.style = Paint.Style.STROKE

    //Detect faces
    val image: InputImage = InputImage.fromBitmap(bitmap, 0);
    val result = detector.process(image)
        .addOnSuccessListener { faces ->
            // Task completed successfully
            for (face in faces) {
                val bounds = face.boundingBox
                canvas.drawRect(bounds.toRectF(), paint)
            }
            val fileOut = File(
                context.applicationContext.filesDir,
                fileName
            )

            var fOut = FileOutputStream(fileOut)

            background.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                fOut
            )

            fOut.flush()
            fOut.close()
            imageView.setImageBitmap(background)
            var numberOfDetectedFaces = faces.size
            callback(numberOfDetectedFaces)
        }
        .addOnFailureListener { _ ->
            // Task failed with an exception
            Log.v(TAG, "Couldn't detect faces")
        }
}

fun contour(context: Context, fileName: String, bitmap: Bitmap, imageView: ImageView){
    val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(realTimeOpts)

    val background: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(background)

    val paint = Paint()
    paint.strokeWidth = 10f
    paint.color = Color.RED
    paint.style = Paint.Style.STROKE

    //Detect faces
    val image: InputImage = InputImage.fromBitmap(background, 0)
    val result = detector.process(image)
        .addOnSuccessListener { faces ->
            for (face in faces) {
                val contours = face.allContours
                for (contour in contours) {
                    val points = contour.points
                    for (i in 0 until points.size - 1) {
                        val startPoint = points[i]
                        val endPoint = points[i + 1]
                        canvas.drawLine(
                            startPoint.x, startPoint.y,
                            endPoint.x, endPoint.y,
                            paint
                        )
                    }
                    val firstPoint = points[0]
                    val lastPoint = points[points.size - 1]
                    canvas.drawLine(
                        lastPoint.x, lastPoint.y,
                        firstPoint.x, firstPoint.y,
                        paint
                    )
                }
            }

            imageView.setImageBitmap(background)
            val fileOut = File(
                context.applicationContext.filesDir,
                fileName
            )

            var fOut = FileOutputStream(fileOut)

            background.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                fOut
            )

            fOut.flush()
            fOut.close()
        }
        .addOnFailureListener { _ ->
            Log.v(TAG, "Couldn't detect faces")
        }
}


fun segment(context: Context, fileName: String, imageView: ImageView, bitmap: Bitmap) {
    val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
        .build()
    val segmenter = Segmentation.getClient(options)

    val image: InputImage = InputImage.fromBitmap(bitmap, 0);
    segmenter.process(image)
        .addOnSuccessListener { segmentationMask ->
            val mask = segmentationMask.buffer
            val maskWidth = segmentationMask.width
            val maskHeight = segmentationMask.height
            @ColorInt val colors = IntArray(maskWidth * maskHeight)
            val maxBg = maskWidth * maskHeight * .95
            var amtBg = 0;
            for (y in 0 until maskHeight) {
                for (x in 0 until maskWidth) {
                    val foregroundChance: Float = mask.getFloat()
                    val backgroundChance = 1 - foregroundChance
                    if (backgroundChance > 0.20) { //128
                        colors[y * maskWidth + x] = Color.argb(255, 255, 0, 255)
                        amtBg += 1
                    }
                }
            }
            if (amtBg > maxBg) { // too much segmenting, probably not selfie
                return@addOnSuccessListener;
            }
            val background: Bitmap = Bitmap.createBitmap(
                colors,
                maskWidth,
                maskHeight,
                Bitmap.Config.ARGB_8888
            )
            val foreground = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(foreground)
            val paint = Paint()

            paint.isAntiAlias = true
            canvas.drawBitmap(background, 0f, 0f, paint)
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OUT))
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            val fileOut = File(
                context.applicationContext.filesDir,
                fileName
            )

            var fOut = FileOutputStream(fileOut)

            foreground.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                fOut
            )

            fOut.flush()
            fOut.close()

            imageView.setImageBitmap(foreground)
        }
        .addOnFailureListener { e ->
            // Task failed with an exception
            // ...
        }
}

fun detectFaceMesh(context: Context, fileName: String,inputBitmap: Bitmap, imageView: ImageView) {

    val detector: FaceMeshDetector
    val optionsBuilder = FaceMeshDetectorOptions.Builder()
    detector = FaceMeshDetection.getClient(optionsBuilder.build())
    val image: InputImage = InputImage.fromBitmap(inputBitmap, 0)
    val bitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bitmap)
    val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    val linePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    detector.process(image).addOnSuccessListener { faceMeshs ->
        for (faceMesh in faceMeshs) {
            val bounds: Rect = faceMesh.boundingBox

            val scaleX =
                bounds.width().toFloat() / (faceMesh.boundingBox.right - faceMesh.boundingBox.left)
            val scaleY =
                bounds.height().toFloat() / (faceMesh.boundingBox.bottom - faceMesh.boundingBox.top)

            for (faceMeshPoint in faceMesh.allPoints) {
                val position = faceMeshPoint.position
                val scaledX = position.x * scaleX
                val scaledY = position.y * scaleY
                canvas.drawCircle(scaledX, scaledY, 5f, pointPaint)
            }

            val triangles: List<Triangle<FaceMeshPoint>> = faceMesh.allTriangles
            for (triangle in triangles) {
                val connectedPoints = triangle.allPoints
                for (i in 0..2) {
                    val start = connectedPoints[i]
                    val end = connectedPoints[(i + 1) % 3]

                    val startScaledX = start.position.x * scaleX
                    val startScaledY = start.position.y * scaleY
                    val endScaledX = end.position.x * scaleX
                    val endScaledY = end.position.y * scaleY

                    canvas.drawLine(startScaledX, startScaledY, endScaledX, endScaledY, linePaint)
                }
            }
        }
        imageView.setImageBitmap(bitmap)
        val fileOut = File(
            context.applicationContext.filesDir,
            fileName
        )

        var fOut = FileOutputStream(fileOut)

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            90,
            fOut
        )

        fOut.flush()
        fOut.close()
    }
}