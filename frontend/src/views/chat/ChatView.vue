<template>
  <section class="chat-page">
    <aside class="session-list">
      <div class="panel-head">
        <h3>消息</h3>
        <div class="panel-head-meta">
          <UnreadBadge v-if="chatNotify.totalUnread" :count="chatNotify.totalUnread" />
          <span class="session-count">{{ sessions.length }}</span>
        </div>
      </div>
      <el-skeleton v-if="loadingSessions" :rows="4" animated />
      <el-empty v-else-if="sessions.length === 0" description="暂无会话" :image-size="72" />
      <button
        v-for="session in sessions"
        :key="session.id"
        class="session-item"
        :class="{ active: session.id === activeSessionId }"
        @click="selectSession(session.id)"
      >
        <div class="avatar-wrap">
          <div class="avatar">{{ peerInitial(session.peerUsername) }}</div>
          <UnreadBadge class="avatar-unread-badge" :count="session.unreadCount" />
        </div>
        <div class="session-body">
          <div class="session-top">
            <strong>{{ session.peerUsername || `用户 ${session.peerId}` }}</strong>
            <span class="session-time">{{ formatMessageTime(session.lastMessageAt || session.createdAt) }}</span>
          </div>
          <div v-if="session.productId" class="session-product">
            <img v-if="session.productImageUrl" :src="resolveMediaUrl(session.productImageUrl)" :alt="session.productTitle" />
            <span>{{ session.productTitle || `商品 #${session.productId}` }}</span>
          </div>
          <div class="session-bottom">
            <p :class="{ unread: session.unreadCount > 0 }">{{ session.lastPreview || '暂无消息' }}</p>
          </div>
        </div>
      </button>
    </aside>

    <main class="chat-panel" v-if="activeSessionId">
      <header class="chat-header">
        <div class="header-left">
          <div class="header-user">
            <div class="avatar large">{{ peerInitial(activePeerName) }}</div>
            <div>
              <h2>{{ activePeerName }}</h2>
              <span class="online-tag" :class="{ offline: !wsConnected }">
                {{ wsConnected ? '在线 · 实时连接' : '离线 · 轮询模式' }}
              </span>
            </div>
          </div>
          <button v-if="activeSession?.productId" type="button" class="chat-product-card" @click="$router.push(`/products/${activeSession.productId}`)">
            <img v-if="activeSession.productImageUrl" :src="resolveMediaUrl(activeSession.productImageUrl)" :alt="activeSession.productTitle" />
            <span v-else class="product-placeholder"><el-icon><Picture /></el-icon></span>
            <span class="chat-product-info">
              <strong>{{ activeSession.productTitle || `商品 #${activeSession.productId}` }}</strong>
              <small>¥{{ money(activeSession.productPrice) }} · {{ productStatusLabel(activeSession.productStatus) }}</small>
            </span>
          </button>
        </div>
        <el-button class="header-report-btn" text type="danger" @click="openReportDrawer('user')">
          举报
        </el-button>
      </header>

      <div ref="messageBoxRef" class="message-box">
        <div v-if="messages.length === 0" class="message-empty">还没有消息，打个招呼吧</div>
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="{ mine: message.senderId === auth.userId }"
        >
          <div class="avatar small">{{ peerInitial(messageDisplayName(message)) }}</div>
          <div class="bubble-wrap">
            <div
              class="bubble"
              :class="{
                recalled: message.status === 'RECALLED',
                'image-bubble': isMediaBubble(message),
              }"
            >
              <template v-if="message.status === 'RECALLED'">
                <p class="recalled-text">消息已撤回</p>
              </template>
              <template v-else-if="message.messageType === 'IMAGE' && message.imageUrl">
                <img
                  :src="resolveMediaUrl(message.imageUrl)"
                  class="chat-img"
                  alt="图片"
                  loading="lazy"
                  @click="openImagePreview(message.imageUrl!)"
                />
              </template>
              <template v-else-if="message.messageType === 'EMOJI'">
                <img
                  v-if="emojiMessageImage(message)"
                  :src="emojiMessageImage(message)!"
                  class="chat-sticker"
                  :alt="emojiMessageLabel(message)"
                  loading="lazy"
                />
                <span v-else class="emoji-fallback">{{ emojiMessageLabel(message) }}</span>
              </template>
              <p v-else class="text-content emoji-text">{{ message.content }}</p>
            </div>
            <div class="meta">
              <span>{{ formatMessageTime(message.createdAt) }}</span>
              <span v-if="message.senderId === auth.userId">{{ deliveryLabel(message) }}</span>
            </div>
          </div>
          <el-dropdown
            v-if="canReportMessage(message)"
            trigger="click"
            class="msg-action"
            @command="() => openReportDrawer('message', message)"
          >
            <el-button class="msg-more-btn" text :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>举报该消息</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <footer class="composer">
        <input ref="fileInputRef" type="file" accept="image/*" hidden @change="handleImageSelect" />
        <div ref="composerRef" class="composer-tools">
          <el-button
            class="composer-tool"
            :icon="Picture"
            circle
            size="large"
            title="发送图片"
            @click="fileInputRef?.click()"
            :loading="uploadingImage"
          />
          <el-button
            class="composer-tool emoji-tool"
            round
            size="large"
            title="表情"
            :type="emojiPickerVisible ? 'primary' : 'default'"
            @click.stop="toggleEmojiPicker"
          >
            <span class="emoji-btn-face">😊</span>
            <span class="emoji-btn-label">表情</span>
          </el-button>
        </div>
        <div ref="composerMainRef" class="composer-main">
          <ChatEmojiPicker
            :visible="emojiPickerVisible"
            @pick-emoji="insertEmoji"
            @pick-sticker="sendSticker"
          />
          <el-input
            v-model="draft"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 6 }"
            placeholder="输入消息，Enter 发送，Shift+Enter 换行"
            :disabled="uploadingImage || sendingSticker"
            resize="none"
            @keydown="handleComposerKeydown"
            @focus="emojiPickerVisible = false"
          />
        </div>
        <el-button
          type="primary"
          size="large"
          class="send-btn"
          :loading="sendingText"
          :disabled="sendingSticker"
          @click="sendText"
        >
          发送
        </el-button>
      </footer>
    </main>

    <main v-else class="chat-empty">
      <el-empty description="选择左侧会话，或从商品详情页联系卖家">
        <el-button type="primary" @click="$router.push('/products')">去商品列表联系卖家</el-button>
      </el-empty>
    </main>

    <ChatReportDrawer
      :visible="reportDrawerVisible"
      :mode="reportMode"
      :message="reportingMessage"
      :peer-id="activePeerId"
      :peer-name="activePeerName"
      :session-id="activeSessionId ?? 0"
      @close="reportDrawerVisible = false"
      @submitted="reportDrawerVisible = false"
    />
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewUrls"
      teleported
      @close="previewVisible = false"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, MoreFilled } from '@element-plus/icons-vue'
import ChatReportDrawer from '../../components/chat/ChatReportDrawer.vue'
import ChatEmojiPicker from '../../components/chat/ChatEmojiPicker.vue'
import UnreadBadge from '../../components/chat/UnreadBadge.vue'
import type { ChatMessage, ChatSession, WsPayload } from '../../stores/chat'
import {
  loadMessages,
  loadSessions,
  readSession,
  sendChatMessage,
  sendWsMessage,
  subscribeChatSocket,
  uploadImage,
} from '../../stores/chat'
import { useAuthStore } from '../../stores/auth'
import { useChatNotifyStore } from '../../stores/chatNotify'
import { resolveSticker, type ChatSticker } from '../../constants/chatEmojis'
import { formatMessageTime, peerInitial, resolveMediaUrl } from '../../utils/media'

const CHAT_MESSAGE_POLL_INTERVAL_MS = 3000

const route = useRoute()
const auth = useAuthStore()
const chatNotify = useChatNotifyStore()

const sessions = ref<ChatSession[]>([])
const messages = ref<ChatMessage[]>([])
const activeSessionId = ref<number | null>(null)
const draft = ref('')
const loadingSessions = ref(false)
const sendingText = ref(false)
const sendingSticker = ref(false)
const uploadingImage = ref(false)
const wsConnected = ref(false)
const emojiPickerVisible = ref(false)
const messageBoxRef = ref<HTMLElement | null>(null)
const composerMainRef = ref<HTMLElement | null>(null)
const composerRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const reportDrawerVisible = ref(false)
const reportMode = ref<'message' | 'user'>('message')
const reportingMessage = ref<ChatMessage | null>(null)
const previewVisible = ref(false)
const previewUrls = ref<string[]>([])
let unsubscribeWs: (() => void) | null = null
let messagePollTimer: ReturnType<typeof setInterval> | null = null

const activePeerName = computed(() => {
  const session = sessions.value.find((item) => item.id === activeSessionId.value)
  return session?.peerUsername || '聊天'
})

const activeSession = computed(() => sessions.value.find((item) => item.id === activeSessionId.value) ?? null)

const activePeerId = computed(() => {
  const session = sessions.value.find((item) => item.id === activeSessionId.value)
  return session?.peerId ?? 0
})

function messageDisplayName(message: ChatMessage) {
  return message.senderId === auth.userId ? auth.displayName : activePeerName.value
}

function deliveryLabel(message: ChatMessage) {
  if (message.senderId !== auth.userId) return ''
  return message.status === 'READ' ? '已读' : '已发送'
}

function isMediaBubble(message: ChatMessage) {
  if (message.status === 'RECALLED') return false
  return message.messageType === 'IMAGE' || message.messageType === 'EMOJI'
}

function emojiMessageImage(message: ChatMessage) {
  if (message.imageUrl) return resolveMediaUrl(message.imageUrl)
  return resolveSticker(message.content)?.imageUrl ?? null
}

function emojiMessageLabel(message: ChatMessage) {
  return resolveSticker(message.content)?.label ?? '[表情]'
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

function productStatusLabel(status?: string) {
  const map: Record<string, string> = {
    ACTIVE: '在售',
    LOCKED: '交易中',
    SOLD: '已售出',
    OFFLINE: '已下架',
    PENDING_REVIEW: '待审核',
  }
  return status ? map[status] ?? status : '商品'
}

function toggleEmojiPicker() {
  emojiPickerVisible.value = !emojiPickerVisible.value
}

function insertEmoji(emoji: string) {
  draft.value += emoji
}

async function sendSticker(sticker: ChatSticker) {
  if (!activeSessionId.value) return
  sendingSticker.value = true
  emojiPickerVisible.value = false
  try {
    const fallback = await sendChatMessage(
      activeSessionId.value,
      'EMOJI',
      sticker.id,
      sticker.imageUrl,
    )
    if (fallback) upsertMessage(fallback)
    await scrollToBottom()
    await refreshSessions()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '表情发送失败')
  } finally {
    sendingSticker.value = false
  }
}

function handleDocumentClick(event: MouseEvent) {
  if (!emojiPickerVisible.value) return
  const target = event.target as Node | null
  if (composerMainRef.value?.contains(target) || composerRef.value?.contains(target)) return
  emojiPickerVisible.value = false
}

function upsertMessage(message: ChatMessage) {
  const index = messages.value.findIndex((item) => item.id === message.id)
  if (index >= 0) {
    messages.value[index] = message
  } else {
    messages.value.push(message)
  }
  messages.value.sort((a, b) => a.seqNo - b.seqNo)
}

async function refreshSessions(silent = false) {
  if (!silent) loadingSessions.value = true
  try {
    sessions.value = await loadSessions()
    chatNotify.applySessions(sessions.value)
  } catch (error) {
    if (!silent) ElMessage.error(error instanceof Error ? error.message : '加载会话失败')
  } finally {
    if (!silent) loadingSessions.value = false
  }
}

async function pollActiveSession() {
  if (!activeSessionId.value) return
  const sessionId = activeSessionId.value
  try {
    const currentLastId = messages.value.at(-1)?.id
    const latestMessages = await loadMessages(sessionId)
    const latestLastId = latestMessages.at(-1)?.id
    messages.value = latestMessages
    if (latestLastId && latestLastId !== currentLastId) {
      await scrollToBottom()
    }
    await readSession(sessionId)
    sendWsMessage({ type: 'READ_RECEIPT', sessionId })
    await refreshSessions(true)
  } catch {
    // polling is a silent fallback; WebSocket and manual actions still surface errors
  }
}

async function selectSession(sessionId: number) {
  activeSessionId.value = sessionId
  try {
    messages.value = await loadMessages(sessionId)
    await readSession(sessionId)
    sendWsMessage({ type: 'READ_RECEIPT', sessionId })
    await refreshSessions()
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载消息失败')
  }
}

function openImagePreview(imageUrl: string) {
  previewUrls.value = [resolveMediaUrl(imageUrl)]
  previewVisible.value = true
}

function handleComposerKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendText()
  }
}

async function sendText() {
  if (!activeSessionId.value || !draft.value.trim()) return
  sendingText.value = true
  try {
    const fallback = await sendChatMessage(activeSessionId.value, 'TEXT', draft.value.trim())
    if (fallback) upsertMessage(fallback)
    draft.value = ''
    await scrollToBottom()
    await refreshSessions()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发送失败')
  } finally {
    sendingText.value = false
  }
}

async function handleImageSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !activeSessionId.value) return
  uploadingImage.value = true
  try {
    const url = await uploadImage(file)
    const fallback = await sendChatMessage(activeSessionId.value, 'IMAGE', '[图片]', url)
    if (fallback) upsertMessage(fallback)
    await scrollToBottom()
    await refreshSessions()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '图片发送失败')
  } finally {
    uploadingImage.value = false
    input.value = ''
  }
}

function canReportMessage(message: ChatMessage) {
  return message.senderId !== auth.userId && message.status !== 'RECALLED'
}

function openReportDrawer(mode: 'message' | 'user', message?: ChatMessage) {
  if (!activeSessionId.value) return
  reportMode.value = mode
  reportingMessage.value = mode === 'message' ? (message ?? null) : null
  reportDrawerVisible.value = true
}

function handleWsEvent(payload: WsPayload) {
  if (payload.type === 'SOCKET_OPEN') {
    wsConnected.value = true
    return
  }
  if (payload.type === 'SOCKET_CLOSE') {
    wsConnected.value = false
    return
  }
  if (payload.type === 'ERROR') {
    ElMessage.error(payload.errorMessage || '消息发送失败')
    return
  }
  if (payload.message && (!activeSessionId.value || payload.message.sessionId === activeSessionId.value)) {
    upsertMessage(payload.message)
    scrollToBottom()
    if (payload.type === 'CHAT_MESSAGE' && payload.message.senderId !== auth.userId && activeSessionId.value) {
      readSession(activeSessionId.value)
      sendWsMessage({ type: 'READ_RECEIPT', sessionId: activeSessionId.value })
    }
    refreshSessions()
  }
  if (payload.type === 'READ_RECEIPT' && payload.messages) {
    payload.messages.forEach((message) => upsertMessage(message))
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messageBoxRef.value) {
    messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
  }
}

onMounted(async () => {
  document.addEventListener('click', handleDocumentClick)
  unsubscribeWs = subscribeChatSocket(handleWsEvent)
  messagePollTimer = setInterval(() => {
    void pollActiveSession()
  }, CHAT_MESSAGE_POLL_INTERVAL_MS)
  await refreshSessions()
  const querySessionId = Number(route.query.sessionId)
  if (querySessionId) {
    await selectSession(querySessionId)
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
  unsubscribeWs?.()
  unsubscribeWs = null
  if (messagePollTimer) {
    clearInterval(messagePollTimer)
    messagePollTimer = null
  }
})

watch(
  () => route.query.sessionId,
  async (value) => {
    const sessionId = Number(value)
    if (sessionId) {
      await selectSession(sessionId)
    }
  },
)
</script>

<style scoped>
.chat-page {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  height: calc(100vh - 100px);
  max-width: 1400px;
  margin: 0 auto;
}
.session-list,
.chat-panel,
.chat-empty {
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--bfu-border);
  box-shadow: 0 10px 30px rgba(7, 59, 42, 0.05);
}
.session-list {
  padding: 18px;
  overflow: auto;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.panel-head h3 {
  margin: 0;
  font-size: 18px;
}
.panel-head-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.session-count {
  background: var(--bfu-green-100);
  color: var(--bfu-green-800);
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.session-item {
  width: 100%;
  display: flex;
  gap: 12px;
  text-align: left;
  border: 1px solid transparent;
  background: var(--bfu-mint-50);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: 0.2s ease;
}
.session-item:hover,
.session-item.active {
  border-color: var(--bfu-green-500);
  background: var(--bfu-green-100);
}
.session-body {
  flex: 1;
  min-width: 0;
}
.session-top,
.session-bottom {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}
.session-top strong {
  font-size: 15px;
}
.session-time {
  color: var(--bfu-muted);
  font-size: 12px;
  white-space: nowrap;
}
.session-product {
  display: grid;
  grid-template-columns: 34px 1fr;
  align-items: center;
  gap: 7px;
  margin-top: 7px;
  color: var(--bfu-green-800);
  font-size: 12px;
  font-weight: 700;
}
.session-product img {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--bfu-leaf-50);
}
.session-product span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-bottom p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-bottom p.unread {
  color: #0f172a;
  font-weight: 600;
}
.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.avatar-unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
}
.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.chat-header {
  padding: 18px 20px;
  border-bottom: 1px solid var(--bfu-border);
  background: linear-gradient(180deg, #ffffff 0%, var(--bfu-mint-50) 100%);
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}
.header-report-btn {
  font-weight: 600;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.chat-header h2 {
  margin: 0;
  font-size: 18px;
}
.online-tag {
  color: var(--bfu-green-600);
  font-size: 12px;
}
.online-tag.offline {
  color: #94a3b8;
}
.chat-product-card {
  min-width: 220px;
  max-width: 420px;
  display: grid;
  grid-template-columns: 52px 1fr;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  padding: 7px;
  text-align: left;
  cursor: pointer;
}
.chat-product-card:hover {
  border-color: var(--bfu-green-500);
}
.chat-product-card img,
.product-placeholder {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--bfu-leaf-50);
}
.product-placeholder {
  display: grid;
  place-items: center;
  color: var(--bfu-green-300);
}
.chat-product-info {
  min-width: 0;
}
.chat-product-info strong,
.chat-product-info small {
  display: block;
}
.chat-product-info strong {
  overflow: hidden;
  color: var(--bfu-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chat-product-info small {
  margin-top: 4px;
  color: var(--bfu-muted);
}
.message-box {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f2f7f1;
}
.message-empty {
  text-align: center;
  color: #94a3b8;
  padding: 48px 0;
}
.message-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.message-row:hover .msg-action {
  opacity: 1;
}
.msg-action {
  align-self: center;
  opacity: 0;
  transition: opacity 0.15s ease;
  flex-shrink: 0;
}
.msg-more-btn {
  padding: 4px;
  color: #94a3b8;
}
.message-row.mine {
  flex-direction: row-reverse;
}
.bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: min(68%, 380px);
  min-width: 0;
}
.message-row.mine .bubble-wrap {
  align-items: flex-end;
}
.message-row:not(.mine) .bubble-wrap {
  align-items: flex-start;
}
.bubble {
  display: inline-block;
  width: fit-content;
  max-width: 100%;
  background: #fff;
  border-radius: 16px 16px 16px 4px;
  padding: 9px 13px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
  word-break: break-word;
}
.bubble.image-bubble {
  padding: 2px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  line-height: 0;
}
.message-row.mine .bubble {
  background: var(--bfu-green-600);
  color: #fff;
  border-radius: 16px 16px 4px 16px;
}
.message-row.mine .bubble.image-bubble {
  background: #fff;
  color: inherit;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.1);
}
.message-row.mine .bubble:has(.chat-sticker) {
  background: transparent;
  box-shadow: none;
  padding: 0;
}
.bubble.recalled {
  background: #f1f5f9;
  color: #94a3b8;
}
.message-row.mine .bubble.recalled {
  background: #e2e8f0;
  color: #64748b;
}
.recalled-text {
  margin: 0;
  font-style: italic;
}
.text-content {
  margin: 0;
  line-height: 1.5;
  font-size: 15px;
  white-space: pre-wrap;
}
.text-content.emoji-text {
  font-size: 22px;
  line-height: 1.4;
}
.chat-sticker {
  display: block;
  width: 120px;
  height: 120px;
  object-fit: contain;
}
.emoji-fallback {
  font-size: 14px;
  color: #64748b;
}
.chat-img {
  display: block;
  max-width: min(320px, 100%);
  max-height: 260px;
  width: auto;
  height: auto;
  border-radius: 10px;
  cursor: zoom-in;
  object-fit: contain;
}
.meta {
  display: flex;
  gap: 6px;
  margin-top: 4px;
  padding: 0 2px;
  color: #94a3b8;
  font-size: 11px;
  line-height: 1.2;
}
.message-row.mine .meta {
  justify-content: flex-end;
}
.composer {
  padding: 14px 16px 16px;
  border-top: 1px solid var(--bfu-border);
  display: flex;
  gap: 10px;
  align-items: flex-end;
  background: #fff;
  border-radius: 0 0 8px 8px;
  flex-shrink: 0;
}
.composer-tools {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-bottom: 2px;
}
.composer-main {
  flex: 1;
  min-width: 0;
  position: relative;
}
.composer-main :deep(.el-textarea__inner) {
  min-height: 72px !important;
  padding: 12px 14px;
  font-size: 15px;
  line-height: 1.5;
  border-radius: 12px;
  box-shadow: none;
}
.composer-tool {
  flex-shrink: 0;
}
.emoji-tool {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 14px !important;
  height: 40px !important;
}
.emoji-btn-face {
  font-size: 20px;
  line-height: 1;
}
.emoji-btn-label {
  font-size: 14px;
  font-weight: 600;
}
.send-btn {
  flex-shrink: 0;
  min-width: 72px;
  height: 44px;
  margin-bottom: 2px;
  border-radius: 12px;
}
.chat-empty {
  display: grid;
  place-items: center;
  min-height: 0;
}
.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--bfu-green-100), #bfe8cc);
  color: var(--bfu-green-800);
  display: grid;
  place-items: center;
  font-weight: 700;
  flex-shrink: 0;
}
.avatar.large {
  width: 46px;
  height: 46px;
  font-size: 18px;
}
.avatar.small {
  width: 34px;
  height: 34px;
  font-size: 13px;
}
@media (max-width: 900px) {
  .chat-page {
    grid-template-columns: 1fr;
    height: calc(100vh - 88px);
  }
  .session-list {
    max-height: 200px;
  }
  .chat-header,
  .header-left {
    align-items: stretch;
    flex-direction: column;
  }
  .chat-product-card {
    max-width: none;
    width: 100%;
  }
  .bubble-wrap {
    max-width: min(82%, 320px);
  }
  .msg-action {
    opacity: 1;
  }
}
</style>
