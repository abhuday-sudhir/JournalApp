import { useEffect, useMemo, useState } from 'react'
import './App.css'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
const TOKEN_KEY = 'journal_jwt_token'

const emptyEntryForm = { title: '', content: '' }
const emptyRegisterForm = {
  username: '',
  email: '',
  password: '',
  sentimentAnalysis: false,
}
const emptyLoginForm = { username: '', password: '' }
const emptyStatus = { text: '', type: 'info' }

function App() {
  const [token, setToken] = useState(localStorage.getItem(TOKEN_KEY) || '')
  const [authMode, setAuthMode] = useState('login')
  const [loginForm, setLoginForm] = useState(emptyLoginForm)
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm)
  const [entries, setEntries] = useState([])
  const [entryForm, setEntryForm] = useState(emptyEntryForm)
  const [editingId, setEditingId] = useState(null)
  const [editingForm, setEditingForm] = useState(emptyEntryForm)
  const [loadingEntries, setLoadingEntries] = useState(false)
  const [status, setStatus] = useState(emptyStatus)
  const isAuthenticated = useMemo(() => Boolean(token), [token])

  function setStatusMessage(text, type = 'info') {
    setStatus({ text, type })
  }

  function normalizeErrorMessage(message) {
    return message
      .replace(/suername/gi, 'username')
      .replace(/passwrod/gi, 'password')
      .replace(/incoorect/gi, 'incorrect')
      .replace(/\berros\b/gi, 'errors')
  }

  function normalizeObjectId(value) {
    if (!value) return ''
    if (typeof value === 'string') {
      const directMatch = value.match(/[a-f0-9]{24}/i)
      return directMatch?.[0] || value.trim()
    }

    if (typeof value === 'object') {
      if (typeof value.$oid === 'string') return value.$oid
      if (typeof value.oid === 'string') return value.oid
      if (typeof value.id === 'string' && value.id.length >= 12) return value.id
      if (typeof value.hexString === 'string') return value.hexString
      if (typeof value.value === 'string') {
        const valueMatch = value.value.match(/[a-f0-9]{24}/i)
        if (valueMatch?.[0]) return valueMatch[0]
      }
      if (typeof value.toHexString === 'function') return value.toHexString()
      if (typeof value.toString === 'function') {
        const stringifiedValue = value.toString()
        const stringMatch = stringifiedValue.match(/[a-f0-9]{24}/i)
        if (stringMatch?.[0]) return stringMatch[0]
      }
      const asJson = JSON.stringify(value)
      const hexMatch = asJson.match(/[a-f0-9]{24}/i)
      if (hexMatch?.[0]) return hexMatch[0]
    }

    const stringValue = String(value)
    if (/^[a-f0-9]{24}$/i.test(stringValue)) return stringValue
    return ''
  }

  function findObjectIdInEntry(entry) {
    try {
      const serialized = JSON.stringify(entry)
      const match = serialized.match(/[a-f0-9]{24}/i)
      return match?.[0] || ''
    } catch {
      return ''
    }
  }

  function normalizeEntry(entry) {
    const resolvedId =
      normalizeObjectId(entry?.id) ||
      normalizeObjectId(entry?.journalId) ||
      normalizeObjectId(entry?.entryId) ||
      normalizeObjectId(entry?.journalID) ||
      normalizeObjectId(entry?._id) ||
      findObjectIdInEntry(entry)
    return {
      ...entry,
      id: resolvedId || entry?.id || entry?._id || '',
      __resolvedId: resolvedId || '',
    }
  }

  function extractHtmlErrorMessage(htmlText) {
    if (!/<html[\s>]/i.test(htmlText)) return ''
    const titleMatch = htmlText.match(/<title[^>]*>(.*?)<\/title>/i)
    if (titleMatch?.[1]) {
      return normalizeErrorMessage(titleMatch[1].replace(/\s+/g, ' ').trim())
    }
    return 'Request failed.'
  }

  function formatRequestError(error, fallbackMessage) {
    if (error instanceof TypeError) {
      return 'Cannot reach backend API. Ensure backend is running on localhost:8080 and restart frontend dev server.'
    }

    const rawMessage = normalizeErrorMessage(error?.message || '')
    if (!rawMessage) return fallbackMessage

    try {
      const parsed = JSON.parse(rawMessage)
      const backendMessage =
        parsed?.message || parsed?.error || parsed?.detail || parsed?.title || ''
      if (backendMessage) {
        const normalizedBackendMessage = normalizeErrorMessage(backendMessage)
        if (/incorrect\s+username\s+or\s+password/i.test(normalizedBackendMessage)) {
          return 'Incorrect username or password.'
        }
        return normalizedBackendMessage
      }
    } catch {
      // Keep raw message if it is plain text.
    }

    const htmlMessage = extractHtmlErrorMessage(rawMessage)
    if (htmlMessage) {
      if (/400\s*[–-]?\s*bad request/i.test(htmlMessage)) {
        return 'Could not delete entry due to invalid request. Please refresh and try again.'
      }
      return htmlMessage
    }

    if (/user\s*not\s*found/i.test(rawMessage)) {
      return 'User not found. Please register first.'
    }
    if (/not found/i.test(rawMessage)) {
      return 'Entry not found. Please refresh and try again.'
    }
    if (/incorrect\s+username\s+or\s+password/i.test(rawMessage)) {
      return 'Incorrect username or password.'
    }

    return rawMessage || fallbackMessage
  }

  async function apiRequest(path, options = {}, requiresAuth = true) {
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    }

    if (requiresAuth && token) {
      headers.Authorization = `Bearer ${token}`
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
    })

    const text = await response.text()

    if (!response.ok) {
      const requestError = new Error(text || `Request failed with status ${response.status}.`)
      requestError.status = response.status
      throw requestError
    }

    return text
  }

  async function loadEntries() {
    if (!token) return
    setLoadingEntries(true)
    try {
      const text = await apiRequest('/journal')
      const data = text ? JSON.parse(text) : []
      const normalizedEntries = Array.isArray(data) ? data.map(normalizeEntry) : []
      setEntries(normalizedEntries)
    } catch (error) {
      setEntries([])
      setStatusMessage(formatRequestError(error, 'Could not load entries.'), 'error')
    } finally {
      setLoadingEntries(false)
    }
  }

  useEffect(() => {
    if (token) {
      loadEntries()
    }
  }, [token])

  function saveToken(newToken) {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    setToken('')
    setEntries([])
    setStatusMessage('Logged out successfully.', 'success')
    setEditingId(null)
    setEditingForm(emptyEntryForm)
  }

  async function handleRegister(event) {
    event.preventDefault()
    setStatus(emptyStatus)
    try {
      await apiRequest(
        '/public/signup',
        {
          method: 'POST',
          body: JSON.stringify(registerForm),
        },
        false,
      )
      setRegisterForm(emptyRegisterForm)
      setAuthMode('login')
      setStatusMessage('Registered successfully. Please login.', 'success')
    } catch (error) {
      setStatusMessage(formatRequestError(error, 'Register failed.'), 'error')
    }
  }

  async function handleLogin(event) {
    event.preventDefault()
    setStatus(emptyStatus)
    try {
      const jwt = await apiRequest(
        '/public/login',
        {
          method: 'POST',
          body: JSON.stringify(loginForm),
        },
        false,
      )
      if (!jwt) {
        throw new Error('No JWT received from server.')
      }
      saveToken(jwt)
      setLoginForm(emptyLoginForm)
      setStatusMessage('Login successful.', 'success')
    } catch (error) {
      setStatusMessage(formatRequestError(error, 'Login failed.'), 'error')
    }
  }

  async function handleCreateEntry(event) {
    event.preventDefault()
    setStatus(emptyStatus)
    try {
      await apiRequest('/journal', {
        method: 'POST',
        body: JSON.stringify(entryForm),
      })
      setEntryForm(emptyEntryForm)
      setStatusMessage('Entry created.', 'success')
      loadEntries()
    } catch (error) {
      setStatusMessage(formatRequestError(error, 'Could not create entry.'), 'error')
    }
  }

  function isSameEntry(left, right) {
    return (
      (left?.title || '') === (right?.title || '') &&
      (left?.content || '') === (right?.content || '') &&
      String(left?.date || '') === String(right?.date || '')
    )
  }

  async function resolveEntryIdentifier(entry, entryIndex = -1) {
    const localId = getEntryIdentifier(entry)
    if (localId) return localId

    try {
      const text = await apiRequest('/journal')
      const data = text ? JSON.parse(text) : []
      const normalizedEntries = Array.isArray(data) ? data.map(normalizeEntry) : []
      setEntries(normalizedEntries)

      const byContentMatch = normalizedEntries.find((candidate) => isSameEntry(candidate, entry))
      const matchedByContentId = getEntryIdentifier(byContentMatch)
      if (matchedByContentId) return matchedByContentId

      if (entryIndex >= 0 && normalizedEntries[entryIndex]) {
        const matchedByIndexId = getEntryIdentifier(normalizedEntries[entryIndex])
        if (matchedByIndexId) return matchedByIndexId
      }
    } catch {
      // Ignore refresh errors and return empty identifier.
    }

    return ''
  }

  async function startEdit(entry, entryIndex = -1) {
    const entryId = await resolveEntryIdentifier(entry, entryIndex)
    if (!entryId) {
      setStatusMessage('Could not edit entry: missing entry identifier.', 'error')
      return
    }

    setEditingId(entryId)
    setEditingForm({
      title: entry.title || '',
      content: entry.content || '',
    })
  }

  function cancelEdit() {
    setEditingId(null)
    setEditingForm(emptyEntryForm)
  }

  async function handleUpdateEntry(entry, entryIndex = -1) {
    setStatus(emptyStatus)
    const entryId = await resolveEntryIdentifier(entry, entryIndex)
    if (!entryId) {
      setStatusMessage('Could not update entry: missing entry identifier.', 'error')
      return
    }

    const encodedId = encodeURIComponent(entryId)

    try {
      await apiRequest(`/journal/id/${encodedId}`, {
        method: 'PUT',
        body: JSON.stringify(editingForm),
      })
      setStatusMessage('Entry updated.', 'success')
    } catch (error) {
      if (error?.status === 404) {
        setStatusMessage('Entry not found. Please refresh and try editing again.', 'error')
        loadEntries()
        return
      }
      setStatusMessage(formatRequestError(error, 'Could not update entry.'), 'error')
      return
    }

    cancelEdit()
    loadEntries()
  }

  function getEntryIdentifier(entry) {
    const normalizedIdentifier =
      normalizeObjectId(entry?.__resolvedId) ||
      normalizeObjectId(entry?.id) ||
      normalizeObjectId(entry?.journalId) ||
      normalizeObjectId(entry?.entryId) ||
      normalizeObjectId(entry?.journalID) ||
      normalizeObjectId(entry?._id) ||
      findObjectIdInEntry(entry)

    return normalizedIdentifier || ''
  }

  async function handleDeleteEntry(entry, entryIndex = -1) {
    setStatus(emptyStatus)
    const entryId = await resolveEntryIdentifier(entry, entryIndex)
    if (!entryId) {
      setStatusMessage('Could not delete entry: missing entry identifier.', 'error')
      return
    }

    const encodedId = encodeURIComponent(entryId)

    try {
      await apiRequest(`/journal/id/${encodedId}`, { method: 'DELETE' })
      setStatusMessage('Entry deleted.', 'success')
    } catch (error) {
      if (error?.status === 404) {
        setStatusMessage('Entry not found. It may already be deleted. Refreshing list...', 'error')
        loadEntries()
        return
      }
      setStatusMessage(formatRequestError(error, 'Could not delete entry.'), 'error')
      return
    }

    loadEntries()
  }

  return (
    <div className={`app-shell ${!isAuthenticated ? 'app-shell--auth' : ''}`}>
      <header className="app-header">
        <h1>Journal App</h1>
      </header>

      {status.text && <div className={`status status--${status.type}`}>{status.text}</div>}

      {!isAuthenticated ? (
        <section className="card auth-card">
          <div className="auth-switch">
            <button
              className={authMode === 'login' ? 'active' : ''}
              onClick={() => setAuthMode('login')}
              type="button"
            >
              Login
            </button>
            <button
              className={authMode === 'register' ? 'active' : ''}
              onClick={() => setAuthMode('register')}
              type="button"
            >
              Register
            </button>
          </div>

          {authMode === 'login' ? (
            <form className="form-grid" onSubmit={handleLogin}>
              <label>
                Username
                <input
                  required
                  value={loginForm.username}
                  onChange={(e) =>
                    setLoginForm((prev) => ({ ...prev, username: e.target.value }))
                  }
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  required
                  value={loginForm.password}
                  onChange={(e) =>
                    setLoginForm((prev) => ({ ...prev, password: e.target.value }))
                  }
                />
              </label>
              <button type="submit">Login</button>
            </form>
          ) : (
            <form className="form-grid" onSubmit={handleRegister}>
              <label>
                Username
                <input
                  required
                  value={registerForm.username}
                  onChange={(e) =>
                    setRegisterForm((prev) => ({ ...prev, username: e.target.value }))
                  }
                />
              </label>
              <label>
                Email
                <input
                  type="email"
                  required
                  value={registerForm.email}
                  onChange={(e) =>
                    setRegisterForm((prev) => ({ ...prev, email: e.target.value }))
                  }
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  required
                  value={registerForm.password}
                  onChange={(e) =>
                    setRegisterForm((prev) => ({ ...prev, password: e.target.value }))
                  }
                />
              </label>
              <label className="checkbox">
                <input
                  type="checkbox"
                  checked={registerForm.sentimentAnalysis}
                  onChange={(e) =>
                    setRegisterForm((prev) => ({
                      ...prev,
                      sentimentAnalysis: e.target.checked,
                    }))
                  }
                />
                Enable sentiment analysis
              </label>
              <button type="submit">Register</button>
            </form>
          )}
        </section>
      ) : (
        <>
          <section className="card">
            <div className="toolbar">
              <h2>Create Journal Entry</h2>
              <button type="button" onClick={logout}>
                Logout
              </button>
            </div>
            <form className="form-grid" onSubmit={handleCreateEntry}>
              <label>
                Title
                <input
                  required
                  value={entryForm.title}
                  onChange={(e) =>
                    setEntryForm((prev) => ({ ...prev, title: e.target.value }))
                  }
                />
              </label>
              <label>
                Content
                <textarea
                  rows="4"
                  value={entryForm.content}
                  onChange={(e) =>
                    setEntryForm((prev) => ({ ...prev, content: e.target.value }))
                  }
                />
              </label>
              <button type="submit">Create Entry</button>
            </form>
          </section>

          <section className="card">
            <h2 className="section-title">Your Entries</h2>
            {loadingEntries ? (
              <p>Loading entries...</p>
            ) : entries.length === 0 ? (
              <p>No entries yet.</p>
            ) : (
              <div className="entry-list">
                {entries.map((entry, index) => {
                  const entryId = getEntryIdentifier(entry)

                  return (
                  <article className="entry-item" key={entryId || `${entry.title || 'entry'}-${index}`}>
                    {editingId === entryId ? (
                      <>
                        <input
                          value={editingForm.title}
                          onChange={(e) =>
                            setEditingForm((prev) => ({
                              ...prev,
                              title: e.target.value,
                            }))
                          }
                        />
                        <textarea
                          rows="4"
                          value={editingForm.content}
                          onChange={(e) =>
                            setEditingForm((prev) => ({
                              ...prev,
                              content: e.target.value,
                            }))
                          }
                        />
                        <div className="entry-actions">
                          <button type="button" onClick={() => handleUpdateEntry(entry, index)}>
                            Save
                          </button>
                          <button type="button" onClick={cancelEdit}>
                            Cancel
                          </button>
                        </div>
                      </>
                    ) : (
                      <>
                        <h3>{entry.title}</h3>
                        <p>{entry.content || 'No content'}</p>
                        <small>
                          Created: {entry.date ? new Date(entry.date).toLocaleString() : '-'}
                        </small>
                        <div className="entry-actions">
                          <button type="button" onClick={() => startEdit(entry, index)}>
                            Edit
                          </button>
                          <button type="button" onClick={() => handleDeleteEntry(entry, index)}>
                            Delete
                          </button>
                        </div>
                      </>
                    )}
                  </article>
                  )
                })}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  )
}

export default App
