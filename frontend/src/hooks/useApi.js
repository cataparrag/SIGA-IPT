import { useCallback, useEffect, useState } from 'react'
import api, { errorMessage } from '../api/client'

/**
 * Loads data from the backend when the component mounts (useState + useEffect).
 * Returns { data, loading, error, reload }. Pass null as url to skip loading.
 */
export default function useApi(url) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(Boolean(url))
  const [error, setError] = useState(null)

  const load = useCallback(() => {
    if (!url) return
    setLoading(true)
    setError(null)
    api.get(url)
      .then((response) => setData(response.data))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [url])

  useEffect(() => {
    load()
  }, [load])

  return { data, loading, error, reload: load }
}
