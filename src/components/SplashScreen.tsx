import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';

interface SplashScreenProps {
  onComplete: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onComplete }) => {
  const [phase, setPhase] = useState(0);

  useEffect(() => {
    const t1 = setTimeout(() => setPhase(1), 300);
    const t2 = setTimeout(() => setPhase(2), 800);
    const t3 = setTimeout(() => setPhase(3), 1400);
    const t4 = setTimeout(() => onComplete(), 2200);
    return () => { clearTimeout(t1); clearTimeout(t2); clearTimeout(t3); clearTimeout(t4); };
  }, [onComplete]);

  return (
    <motion.div
      className="fixed inset-0 z-[100] flex flex-col items-center justify-center bg-gamma-bg"
      exit={{ opacity: 0 }}
      transition={{ duration: 0.5 }}
    >
      {/* Background effects */}
      <div className="absolute inset-0 overflow-hidden">
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1.5, opacity: 0.15 }}
          transition={{ duration: 2, ease: 'easeOut' }}
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full"
          style={{
            background: 'radial-gradient(circle, #8b5cf6 0%, #06b6d4 50%, transparent 70%)',
          }}
        />
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 2, opacity: 0.08 }}
          transition={{ duration: 2.5, ease: 'easeOut', delay: 0.3 }}
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] rounded-full"
          style={{
            background: 'radial-gradient(circle, #06b6d4 0%, transparent 60%)',
          }}
        />
      </div>

      {/* Logo */}
      <motion.div
        initial={{ scale: 0.5, opacity: 0 }}
        animate={{ 
          scale: phase >= 1 ? 1 : 0.5, 
          opacity: phase >= 1 ? 1 : 0 
        }}
        transition={{ type: 'spring', stiffness: 200, damping: 20 }}
        className="relative z-10"
      >
        <div className="w-20 h-20 rounded-2xl gradient-primary flex items-center justify-center shadow-2xl shadow-gamma-primary/30">
          <span className="text-3xl font-display font-black text-white tracking-tight">γ</span>
        </div>
      </motion.div>

      {/* Brand Name */}
      <motion.div
        initial={{ y: 20, opacity: 0 }}
        animate={{ 
          y: phase >= 2 ? 0 : 20, 
          opacity: phase >= 2 ? 1 : 0 
        }}
        transition={{ duration: 0.5, delay: 0.1 }}
        className="relative z-10 mt-6 text-center"
      >
        <h1 className="text-3xl font-display font-black text-gamma-text-primary tracking-wider">
          GAMMA
        </h1>
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: phase >= 3 ? 1 : 0 }}
          transition={{ duration: 0.5 }}
          className="text-sm text-gamma-text-secondary mt-2 tracking-wide"
        >
          Your Personal Frequency
        </motion.p>
      </motion.div>

      {/* Loading indicator */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: phase >= 2 ? 1 : 0 }}
        className="absolute bottom-16 flex gap-1.5"
      >
        {[0, 1, 2].map((i) => (
          <motion.div
            key={i}
            animate={{
              scale: [1, 1.3, 1],
              opacity: [0.3, 1, 0.3],
            }}
            transition={{
              duration: 1,
              repeat: Infinity,
              delay: i * 0.15,
            }}
            className="w-1.5 h-1.5 rounded-full bg-gamma-primary"
          />
        ))}
      </motion.div>
    </motion.div>
  );
};
