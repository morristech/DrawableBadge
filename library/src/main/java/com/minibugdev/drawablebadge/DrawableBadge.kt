package com.minibugdev.drawablebadge

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.text.TextPaint

class DrawableBadge private constructor(private val context: Context,
                                        @ColorInt private val textColor: Int,
                                        @ColorInt private val badgeColor: Int,
                                        @ColorInt private val badgeBorderColor: Int,
                                        private val badgeSize: Float,
                                        private val badgePosition: BadgePosition,
                                        private val bitmap: Bitmap) {

	class Builder(private val context: Context) {

		@ColorInt private var textColor: Int? = null
		@ColorInt private var badgeColor: Int? = null
		@ColorInt private var badgeBorderColor: Int? = null
		private var badgeSize: Float? = null
		private var badgePosition: BadgePosition? = null
		private var bitmap: Bitmap? = null

		fun drawableResId(@DrawableRes drawableRes: Int) = apply { this.bitmap = BitmapFactory.decodeResource(context.resources, drawableRes) }

		fun drawable(drawable: Drawable) = apply {
			if (drawable is BitmapDrawable) {
				this.bitmap = drawable.bitmap
			}
		}

		fun bitmap(bitmap: Bitmap) = apply { this.bitmap = bitmap }

		fun textColor(@ColorRes textColorRes: Int) = apply { this.textColor = ContextCompat.getColor(context, textColorRes) }

		fun badgeColor(@ColorRes badgeColorRes: Int) = apply { this.badgeColor = ContextCompat.getColor(context, badgeColorRes) }

		fun badgeBorderColor(@ColorRes badgeBorderColorRes: Int) = apply { this.badgeBorderColor = ContextCompat.getColor(context, badgeBorderColorRes) }

		fun badgeSize(@DimenRes badgeSize: Int) = apply { this.badgeSize = context.resources.getDimensionPixelOffset(badgeSize).toFloat() }

		fun badgePosition(badgePosition: BadgePosition) = apply { this.badgePosition = badgePosition }

		fun build(): DrawableBadge {
			if (bitmap == null) throw IllegalArgumentException("Badge drawable/bitmap can not be null.")
			if (badgeSize == null) badgeSize(R.dimen.default_badge_size)
			if (textColor == null) textColor(R.color.default_badge_text_color)
			if (badgeColor == null) badgeColor(R.color.default_badge_color)
			if (badgeBorderColor == null) badgeBorderColor(R.color.default_badge_border_color)
			if (badgePosition == null) badgePosition(BadgePosition.TOP_RIGHT)

			return DrawableBadge(
				context = context,
				bitmap = bitmap!!,
				textColor = textColor!!,
				badgeColor = badgeColor!!,
				badgeBorderColor = badgeBorderColor!!,
				badgeSize = badgeSize!!,
				badgePosition = badgePosition!!)
		}
	}

	fun get(number: Int): Drawable {
		val resources = context.resources
		if(number == 0) return BitmapDrawable(resources, bitmap)

		val sourceBitmap = bitmap
		val width = sourceBitmap.width
		val height = sourceBitmap.height
		val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

		val canvas = Canvas(output)
		val rect = Rect(0, 0, width, height)
		val paint = Paint().apply {
			isAntiAlias = true
			isFilterBitmap = true
			isDither = true
			textAlign = Paint.Align.CENTER
			color = badgeColor
		}
		canvas.drawBitmap(sourceBitmap, rect, rect, paint)

		val badgeRect = when (badgePosition) {
			BadgePosition.TOP_LEFT     -> RectF(0f, 0f, badgeSize, badgeSize)
			BadgePosition.TOP_RIGHT    -> RectF(width.toFloat() - badgeSize, 0f, width.toFloat(), badgeSize)
			BadgePosition.BOTTOM_LEFT  -> RectF(0f, height.toFloat() - badgeSize, badgeSize, height.toFloat())
			BadgePosition.BOTTOM_RIGHT -> RectF(width.toFloat() - badgeSize, height.toFloat() - badgeSize, width.toFloat(), height.toFloat())
		}
		canvas.drawOval(badgeRect, paint)

		val paintBorder = Paint().apply {
			isAntiAlias = true
			isFilterBitmap = true
			isDither = true
			textAlign = Paint.Align.CENTER
			color = badgeBorderColor
			style = Paint.Style.STROKE
		}
		canvas.drawOval(badgeRect, paintBorder)

		val textSize = badgeRect.height() * 0.55f
		val textPaint = TextPaint().apply {
			this.isAntiAlias = true
			this.color = textColor
			this.textSize = textSize
		}

		val text = number.toString()
		val x = badgeRect.centerX() - (textPaint.measureText(text) / 2f)
		val y = badgeRect.centerY() - (textPaint.ascent() + textPaint.descent()) * 0.5f
		canvas.drawText(text, x, y, textPaint)

		return BitmapDrawable(resources, output)
	}
}
