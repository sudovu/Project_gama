import React, { useRef, useEffect } from 'react';

interface AmbientParticlesProps {
  color?: string;
  count?: number;
  className?: string;
}

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  opacity: number;
  life: number;
  maxLife: number;
}

export const AmbientParticles: React.FC<AmbientParticlesProps> = ({
  color = '#8b5cf6',
  count = 30,
  className = '',
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const particlesRef = useRef<Particle[]>([]);
  const animationRef = useRef<number>(0);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const resize = () => {
      canvas.width = canvas.offsetWidth * 2;
      canvas.height = canvas.offsetHeight * 2;
    };
    resize();

    // Initialize particles
    particlesRef.current = Array(count).fill(null).map(() => ({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: -Math.random() * 0.8 - 0.2,
      size: Math.random() * 3 + 1,
      opacity: Math.random() * 0.5,
      life: Math.random() * 200,
      maxLife: 200 + Math.random() * 100,
    }));

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      particlesRef.current.forEach((p) => {
        p.x += p.vx;
        p.y += p.vy;
        p.life++;

        // Fade in/out
        const lifeRatio = p.life / p.maxLife;
        if (lifeRatio < 0.2) {
          p.opacity = (lifeRatio / 0.2) * 0.6;
        } else if (lifeRatio > 0.8) {
          p.opacity = ((1 - lifeRatio) / 0.2) * 0.6;
        }

        // Reset if out of bounds or life ended
        if (p.life >= p.maxLife || p.y < -10 || p.x < -10 || p.x > canvas.width + 10) {
          p.x = Math.random() * canvas.width;
          p.y = canvas.height + 10;
          p.life = 0;
          p.opacity = 0;
          p.vx = (Math.random() - 0.5) * 0.5;
          p.vy = -Math.random() * 0.8 - 0.2;
        }

        // Draw particle with glow
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        ctx.fillStyle = `${color}${Math.floor(p.opacity * 255).toString(16).padStart(2, '0')}`;
        ctx.fill();

        // Glow effect
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size * 3, 0, Math.PI * 2);
        ctx.fillStyle = `${color}${Math.floor(p.opacity * 40).toString(16).padStart(2, '0')}`;
        ctx.fill();
      });

      animationRef.current = requestAnimationFrame(draw);
    };

    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(canvas);
    animationRef.current = requestAnimationFrame(draw);

    return () => {
      cancelAnimationFrame(animationRef.current);
      resizeObserver.disconnect();
    };
  }, [color, count]);

  return (
    <canvas
      ref={canvasRef}
      className={`absolute inset-0 pointer-events-none ${className}`}
    />
  );
};
