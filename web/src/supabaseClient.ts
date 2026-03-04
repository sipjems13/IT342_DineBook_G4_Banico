import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

// Create client even with placeholder values - they'll be replaced once you add real credentials
export const supabase = createClient(
  supabaseUrl || 'https://placeholder.supabase.co',
  supabaseAnonKey || 'placeholder-key'
)

// Check if credentials are properly configured
export const isSupabaseConfigured = 
  supabaseUrl && 
  supabaseUrl !== 'https://placeholder.supabase.co' &&
  supabaseAnonKey &&
  supabaseAnonKey !== 'placeholder-key'

