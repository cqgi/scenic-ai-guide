<template>
  <section class="stage" :class="[`state-${state}`, `emotion-${emotion}`]">
    <div class="stage-copy">
      <p>{{ scenicName }}</p>
      <h1>小栖</h1>
      <span>{{ statusText }}</span>
    </div>

    <div class="portal" aria-label="AI 数字人小栖">
      <span class="ring ring-one" />
      <span class="ring ring-two" />
      <img class="guide" :src="xiaoqiImage" alt="AI 数字人小栖" />
      <span class="mouth" :style="{ transform: `translateX(-50%) scaleY(${mouthScale})` }" />
      <span class="base-light" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import xiaoqiImage from '@/assets/digital-human/xiaoqi-guide.png'

export type DigitalHumanState = 'idle' | 'listening' | 'thinking' | 'speaking' | 'happy' | 'sorry' | 'error'

const props = defineProps<{
  state: DigitalHumanState
  emotion: string
  scenicName: string
  mouthOpen: number
}>()

const statusText = computed(() => {
  const labels: Record<DigitalHumanState, string> = {
    idle: '待机中',
    listening: '正在聆听',
    thinking: '正在思考',
    speaking: '正在讲解',
    happy: '很高兴帮到你',
    sorry: '当前依据不足',
    error: '请稍后重试',
  }
  return labels[props.state]
})

const mouthScale = computed(() => Math.max(0.2, Math.min(1.8, 0.35 + props.mouthOpen * 1.45)))
</script>

<style scoped>
.stage {
  position: relative;
  min-height: 100%;
  display: grid;
  grid-template-rows: auto 1fr;
  align-items: center;
  justify-items: center;
  gap: 24px;
  padding: 32px 28px;
  overflow: hidden;
}

.stage::before,
.stage::after {
  content: '';
  position: absolute;
  inset: auto auto 4% -15%;
  width: 130%;
  height: 34%;
  background:
    linear-gradient(145deg, transparent 0 26%, rgba(0, 212, 170, 0.08) 27% 30%, transparent 31%),
    linear-gradient(25deg, transparent 0 35%, rgba(201, 169, 110, 0.09) 36% 39%, transparent 40%);
  clip-path: polygon(0 68%, 10% 52%, 24% 64%, 38% 35%, 53% 58%, 70% 28%, 86% 54%, 100% 38%, 100% 100%, 0 100%);
  opacity: 0.8;
  pointer-events: none;
}

.stage::after {
  filter: blur(18px);
  opacity: 0.35;
}

.stage-copy {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #f5f0e8;
}

.stage-copy p {
  margin: 0 0 8px;
  color: rgba(245, 240, 232, 0.58);
  font-size: 13px;
}

.stage-copy h1 {
  margin: 0;
  font-size: clamp(40px, 5vw, 68px);
  line-height: 1;
  font-family: Georgia, 'Songti SC', serif;
  font-weight: 700;
}

.stage-copy span {
  display: inline-flex;
  margin-top: 14px;
  min-height: 30px;
  align-items: center;
  padding: 6px 14px;
  border: 1px solid rgba(0, 212, 170, 0.24);
  border-radius: 999px;
  color: rgba(245, 240, 232, 0.72);
  background: rgba(15, 15, 26, 0.42);
}

.portal {
  position: relative;
  z-index: 1;
  width: min(76vw, 340px);
  aspect-ratio: 0.72;
  display: grid;
  place-items: end center;
  border: 1px solid rgba(0, 212, 170, 0.24);
  border-radius: 48% 48% 24px 24px;
  background:
    radial-gradient(circle at 50% 34%, rgba(0, 212, 170, 0.18), transparent 36%),
    linear-gradient(180deg, rgba(26, 42, 58, 0.8), rgba(15, 15, 26, 0.7));
  box-shadow:
    0 0 70px rgba(0, 212, 170, 0.12),
    inset 0 0 70px rgba(0, 212, 170, 0.06);
  overflow: hidden;
  animation: float 5s ease-in-out infinite;
}

.ring {
  position: absolute;
  inset: 7%;
  border: 1px solid rgba(0, 212, 170, 0.18);
  border-radius: 48% 48% 22px 22px;
  filter: drop-shadow(0 0 18px rgba(0, 212, 170, 0.14));
}

.ring-two {
  inset: 13%;
  border-color: rgba(201, 169, 110, 0.12);
}

.guide {
  position: relative;
  z-index: 2;
  width: 92%;
  margin-bottom: -8%;
  filter: drop-shadow(0 28px 34px rgba(0, 0, 0, 0.34));
  user-select: none;
}

.mouth {
  position: absolute;
  z-index: 4;
  left: 50%;
  top: 45.6%;
  width: 34px;
  height: 15px;
  border-radius: 0 0 999px 999px;
  background: rgba(95, 30, 47, 0.92);
  transform-origin: center top;
  transition: transform 80ms linear;
}

.base-light {
  position: absolute;
  z-index: 3;
  bottom: 3%;
  left: 22%;
  width: 56%;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, #c9a96e, transparent);
  filter: blur(5px);
  animation: baseGlow 2.4s ease-in-out infinite alternate;
}

.state-listening .portal {
  border-color: rgba(201, 169, 110, 0.52);
  box-shadow: 0 0 0 0 rgba(201, 169, 110, 0.22), 0 0 70px rgba(201, 169, 110, 0.13);
  animation: recordingPulse 1.5s ease-out infinite;
}

.state-thinking .ring-one {
  animation: spin 1.8s linear infinite;
}

.state-speaking .portal {
  border-color: rgba(0, 212, 170, 0.5);
  box-shadow:
    0 0 90px rgba(0, 212, 170, 0.18),
    inset 0 0 70px rgba(201, 169, 110, 0.08);
}

.state-happy .portal,
.emotion-happy .portal {
  border-color: rgba(201, 169, 110, 0.46);
}

.state-sorry .portal,
.state-error .portal,
.emotion-sorry .portal {
  border-color: rgba(111, 87, 156, 0.45);
  filter: saturate(0.82);
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes baseGlow {
  from {
    opacity: 0.45;
  }
  to {
    opacity: 1;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes recordingPulse {
  0% {
    box-shadow: 0 0 0 0 rgba(201, 169, 110, 0.24), 0 0 70px rgba(201, 169, 110, 0.13);
  }
  100% {
    box-shadow: 0 0 0 28px rgba(201, 169, 110, 0), 0 0 70px rgba(201, 169, 110, 0.13);
  }
}

@media (max-width: 860px) {
  .stage {
    min-height: 48vh;
    padding: 28px 20px 18px;
  }

  .portal {
    width: min(72vw, 300px);
  }

  .stage-copy h1 {
    font-size: 38px;
  }
}
</style>
