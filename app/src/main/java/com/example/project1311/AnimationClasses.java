package com.example.project1311.animations;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimationClasses {

    public static class CheckMarkView extends View {
        private Paint paint;
        private ValueAnimator checkAnimator;
        private float progress = 0f;
        private int checkColor = Color.GREEN;
        private int strokeWidth = 10;

        public CheckMarkView(Context context) {
            super(context);
            init();
        }

        public CheckMarkView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(checkColor);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true); // Note: Smooths edges for a cleaner look
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            // Note: These points define the checkmark shape (start → middle → end)
            float startX = width * 0.2f;
            float startY = height * 0.5f;
            float middleX = width * 0.4f;
            float middleY = height * 0.7f;
            float endX = width * 0.8f;
            float endY = height * 0.3f;

            Path checkPath = new Path();
            checkPath.moveTo(startX, startY);
            checkPath.lineTo(middleX, middleY);
            checkPath.lineTo(endX, endY);

            // Note: PathMeasure lets us animate by drawing only part of the path
            PathMeasure pathMeasure = new PathMeasure(checkPath, false);
            Path extractedPath = new Path();
            pathMeasure.getSegment(0f, pathMeasure.getLength() * progress, extractedPath, true);

            canvas.drawPath(extractedPath, paint);
        }

        public void startAnimation() {
            if (checkAnimator != null) {
                checkAnimator.cancel(); // Note: Stops any old animation to avoid overlap
            }

            // Note: Animates progress from 0 to 1 over 200ms for a quick, smooth draw
            checkAnimator = ValueAnimator.ofFloat(0f, 1f);
            checkAnimator.setDuration(200);
            checkAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // Note: Makes it speed up then slow down
            checkAnimator.addUpdateListener(animation -> {
                progress = (float) animation.getAnimatedValue();
                invalidate(); // Note: Triggers redraw with updated progress
            });
            checkAnimator.start();
        }

        public void reset() {
            progress = 0f;
            invalidate(); // Note: Resets to undrawn state for reuse
        }
    }

    public static class XMarkView extends View {
        private Paint paint;
        private ValueAnimator xAnimator;
        private float progress = 0f;
        private int xColor = Color.RED;
        private int strokeWidth = 10;

        public XMarkView(Context context) {
            super(context);
            init();
        }

        public XMarkView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(xColor);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            // Note: Two paths for the X: top-left to bottom-right, and top-right to bottom-left
            Path xPath1 = new Path();
            xPath1.moveTo(width * 0.2f, height * 0.2f);
            xPath1.lineTo(width * 0.8f, height * 0.8f);

            Path xPath2 = new Path();
            xPath2.moveTo(width * 0.8f, height * 0.2f);
            xPath2.lineTo(width * 0.2f, height * 0.8f);

            PathMeasure pathMeasure1 = new PathMeasure(xPath1, false);
            PathMeasure pathMeasure2 = new PathMeasure(xPath2, false);

            Path extractedPath1 = new Path();
            Path extractedPath2 = new Path();

            // Note: Both lines animate together based on the same progress value
            pathMeasure1.getSegment(0f, pathMeasure1.getLength() * progress, extractedPath1, true);
            pathMeasure2.getSegment(0f, pathMeasure2.getLength() * progress, extractedPath2, true);

            canvas.drawPath(extractedPath1, paint);
            canvas.drawPath(extractedPath2, paint);
        }

        public void startAnimation() {
            if (xAnimator != null) {
                xAnimator.cancel();
            }

            xAnimator = ValueAnimator.ofFloat(0f, 1f);
            xAnimator.setDuration(200);
            xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            xAnimator.addUpdateListener(animation -> {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            });
            xAnimator.start();
        }

        public void reset() {
            progress = 0f;
            invalidate();
        }
    }

    public static class WinningAnimationView extends View {
        private Paint paint;
        private Paint textPaint;
        private List<Particle> particles;
        private Random random;
        private ValueAnimator animator;
        private ValueAnimator textAnimator;
        private float textScale = 1.0f;
        private final int[] FESTIVE_COLORS = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.WHITE
        };

        public WinningAnimationView(Context context) {
            super(context);
            init();
        }

        public WinningAnimationView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL); // Note: Particles are filled circles, not outlines

            // Note: Big, bold, centered text with a shadow for pop
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(120);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setShadowLayer(10, 0, 0, Color.BLACK);

            particles = new ArrayList<>();
            random = new Random();

            // Note: 150 particles for a busy, festive effect—could reduce for performance
            for (int i = 0; i < 150; i++) {
                particles.add(new Particle());
            }

            startAnimation();
        }

        private void startAnimation() {
            // Note: Particle animation runs forever, updating every 1500ms
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(animation -> {
                for (Particle particle : particles) {
                    particle.update();
                }
                invalidate();
            });
            animator.start();

            // Note: Text scales up and down (1 to 1.2) for a pulsing effect
            textAnimator = ValueAnimator.ofFloat(1f, 1.2f);
            textAnimator.setDuration(1000);
            textAnimator.setRepeatCount(ValueAnimator.INFINITE);
            textAnimator.setRepeatMode(ValueAnimator.REVERSE);
            textAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            textAnimator.addUpdateListener(animation -> {
                textScale = (float) animation.getAnimatedValue();
                invalidate();
            });
            textAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Note: Draw each particle with its own color and fading alpha
            for (Particle particle : particles) {
                paint.setColor(particle.color);
                paint.setAlpha(particle.alpha);
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint);
            }

            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;

            // Note: Scale the text around its center for a smooth pulse
            canvas.save();
            canvas.scale(textScale, textScale, centerX, centerY);
            canvas.drawText("YOU WIN!", centerX, centerY, textPaint);
            canvas.restore();
        }

        private class Particle {
            float x, y;
            float speedX, speedY;
            float radius;
            int color;
            int alpha;
            float angle;
            float speed;

            Particle() {
                reset();
            }

            void reset() {
                // Note: Start particles at the center, then scatter outward
                x = getWidth() / 2f;
                y = getHeight() / 2f;
                angle = random.nextFloat() * (float) (2 * Math.PI); // Note: Random direction in radians
                speed = random.nextFloat() * 15 + 5; // Note: Speed between 5 and 20
                speedX = (float) Math.cos(angle) * speed;
                speedY = (float) Math.sin(angle) * speed;
                radius = random.nextFloat() * 12 + 4; // Note: Size between 4 and 16
                color = FESTIVE_COLORS[random.nextInt(FESTIVE_COLORS.length)];
                alpha = 255;
            }

            void update() {
                x += speedX;
                y += speedY;
                alpha = Math.max(0, alpha - 3); // Note: Fade out gradually
                speedY += 0.1f; // Note: Adds a slight "gravity" effect

                // Note: Reset if faded or off-screen to keep the animation going
                if (alpha <= 0 || x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
                    reset();
                }
            }
        }

        public void stopAnimation() {
            if (animator != null) {
                animator.cancel();
            }
            if (textAnimator != null) {
                textAnimator.cancel(); // Note: Stops both animations when hiding the view
            }
        }
    }
}