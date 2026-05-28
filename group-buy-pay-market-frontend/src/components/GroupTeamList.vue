<template>
  <div class="group-list">
    <div v-if="teams.length === 0" class="group-item empty-tips">
      小伙伴，赶紧去开团吧，做村里最靓的仔！
    </div>

    <div v-for="team in teams" :key="team.teamId" class="group-item">
      <div>
        <div class="user-info">{{ obfuscateUserId(team.userId) }}</div>
        <div class="group-status">
          <span>组队仅剩{{ team.remainingCount }}人，拼单即将结束</span>
          <span class="countdown">{{ team.validTimeCountdown }}</span>
        </div>
      </div>
      <button class="group-btn" :disabled="team.isExpired" @click="handleJoin(team)">
        {{ team.isExpired ? '已结束' : '参与拼团' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { obfuscateUserId } from '../lib/market'
import type { TeamSummary } from '../types/api'

defineProps<{
  teams: TeamSummary[]
}>()

const emit = defineEmits<{
  (e: 'join', teamId: string): void
}>()

function handleJoin(team: TeamSummary) {
  if (team.isExpired) {
    return
  }

  emit('join', team.teamId)
}
</script>

<style scoped>
.group-list {
  display: grid;
  gap: 16px;
}

.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.group-status {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  color: #6b7280;
}

.group-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
}

.group-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.empty-tips {
  justify-content: center;
  color: #6b7280;
}
</style>
