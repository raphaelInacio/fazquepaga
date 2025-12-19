import React from 'react';
import { cn } from "@/lib/utils";

// Explicit imports for Vite to bundle assets correctly
import mascotWaving from '@/assets/mascot/mascot_waving.png';
import mascotWorking from '@/assets/mascot/mascot_working.png';
import mascotCelebrating from '@/assets/mascot/mascot_celebrating.png';
import mascotTech from '@/assets/mascot/mascot_tech.png';
import mascotTeaching from '@/assets/mascot/mascot_teaching.png';
import mascotRich from '@/assets/mascot/mascot_rich.png';
import mascotPresenting from '@/assets/mascot/mascot_presenting.png';
import mascotPeeking from '@/assets/mascot/mascot_peeking.png';
import defaultMascot from '@/assets/mascot/faz.png';

export type MascotState =
    | 'waving'
    | 'working'
    | 'celebrating'
    | 'tech'
    | 'teaching'
    | 'rich'
    | 'presenting'
    | 'peeking'
    | 'default';

interface MascotProps {
    state: MascotState;
    className?: string;
    width?: number;
    height?: number;
    alt?: string;
}

// Map states to imported assets
const MASCOT_ASSETS: Record<MascotState, string> = {
    waving: mascotWaving,
    working: mascotWorking,
    celebrating: mascotCelebrating,
    tech: mascotTech,
    teaching: mascotTeaching,
    rich: mascotRich,
    presenting: mascotPresenting,
    peeking: mascotPeeking,
    default: defaultMascot,
};

export function Mascot({ state, className, width, height, alt }: MascotProps) {
    const src = MASCOT_ASSETS[state];
    const defaultAlt = `Mascot ${state}`;

    // Default animations based on state
    const getAnimationClass = (state: MascotState) => {
        switch (state) {
            case 'waving':
            case 'presenting':
                return 'animate-fade-in-up';
            case 'celebrating':
                return 'animate-bounce';
            case 'tech':
                return 'animate-pulse';
            case 'peeking':
                return 'animate-in slide-in-from-bottom duration-1000';
            default:
                return 'animate-fade-in';
        }
    };

    return (
        <div className={cn("relative z-10", className)}>
            <img
                src={src}
                alt={alt || defaultAlt}
                width={width}
                height={height}
                className={cn(
                    "object-contain transition-all duration-500",
                    getAnimationClass(state)
                )}
                style={{ width: width ? `${width}px` : 'auto', height: height ? `${height}px` : 'auto' }}
            />
        </div>
    );
}
