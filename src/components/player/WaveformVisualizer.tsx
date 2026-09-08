import React, { useRef, useEffect, useCallback } from 'react';

interface WaveformVisualizerProps {
  isPlaying: boolean;
  color?: string;
  className?: string;
  barCount?: number;
}

export const WaveformVisualizer: React.FC<WaveformVisualizerProps> = ({
  isPlaying,
  color = '#8b5cf6',
  className = '',
  barCount = 48,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animationRef = useRef<number>(0);
  const barsRef = useRef<number[]>(Array(barCount).fill(0).map(() => Math.random() * 0.3));
  const targetRef = useRef<number[]>(Array(barCount).fill(0));

  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const { width, height } = canvas;
    ctx.clearRect(0, 0, width, height);

    const barWidth = width / barCount;
    const gap = 2;
    const actualBarWidth = barWidth - gap;

    for (let i = 0; i < barCount; i++) {
      // Smooth interpolation
      const target = isPlaying ? targetRef.current[i] : 0.05;
      barsRef.current[i] += (target - barsRef.current[i]) * 0.15;

      const barHeight = Math.max(2, barsRef.current[i] * height);
      const x = i * barWidth;
      const y = height - barHeight;

      // Gradient per bar
      const gradient = ctx.createLinearGradient(0, y, 0, height);
      gradient.addColorStop(0, color);
      gradient.addColorStop(1, `${color}40`);

      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.roundRect(x, y, actualBarWidth, barHeight, 2);
      ctx.fill();
    }

    if (isPlaying) {
      // Update targets periodically
      if (Math.random() > 0.7) {
        const idx = Math.floor(Math.random() * barCount);
        targetRef.current[idx] = 0.2 + Math.random() * 0.8;
      }
      // Decay targets
      for (let i = 0; i < barCount; i++) {
        targetRef.current[i] *= 0.95;
        if (targetRef.current[i] < 0.05) {
          targetRef.current[i] = 0.1 + Math.random() * 0.4;
        }
      }
    }

    animationRef.current = requestAnimationFrame(draw);
  }, [isPlaying, color, barCount]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const resizeObserver = new ResizeObserver(() => {
      canvas.width = canvas.offsetWidth * 2;
      canvas.height = canvas.offsetHeight * 2;
    });
    resizeObserver.observe(canvas);

    canvas.width = canvas.offsetWidth * 2;
    canvas.height = canvas.offsetHeight * 2;

    animationRef.current = requestAnimationFrame(draw);

    return () => {
      cancelAnimationFrame(animationRef.current);
      resizeObserver.disconnect();
    };
  }, [draw]);

  return (
    <canvas
      ref={canvasRef}
      className={`w-full h-full ${className}`}
      style={{ imageRendering: 'auto' }}
    />
  );
};
