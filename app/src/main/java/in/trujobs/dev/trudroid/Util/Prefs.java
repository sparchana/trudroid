package in.trujobs.dev.trudroid.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Prefs {
    private static final File sFile = new File("tjobs");

    public static final File.SharedPreference<String> firstName = sFile.stringValue("firstName", "");
    public static final File.SharedPreference<String> lastName = sFile.stringValue("lastName", "");
    public static final File.SharedPreference<String> candidateMobile = sFile.stringValue("candidateMobile", "");
    public static final File.SharedPreference<Long> leadId = sFile.longValue("leadId", 0L);
    public static final File.SharedPreference<Long> candidateId = sFile.longValue("candidateId", 0L);
    public static final File.SharedPreference<Integer> candidateMinProfile = sFile.intValue("candidateMinProfile", 0);
    public static final File.SharedPreference<Integer> candidateGender = sFile.intValue("candidateGender", 0);
    public static final File.SharedPreference<Integer> isAssessed = sFile.intValue("isAssessed", 0);
    public static final File.SharedPreference<String> sessionId = sFile.stringValue("sessionId", "");
    public static final File.SharedPreference<Long> sessionExpiry = sFile.longValue("sessionExpiry", 0L);

    public static final File.SharedPreference<Integer> storedOtp = sFile.intValue("storedOtp", 0);
    public static final File.SharedPreference<Long> jobPostId = sFile.longValue("jobPostId", 0L);
    public static final File.SharedPreference<String> candidateHomeLocalityName = sFile.stringValue("candidateHomeLocalityName", "");
    public static final File.SharedPreference<String> candidateHomeLat = sFile.stringValue("candidateHomeLat", "0.0");
    public static final File.SharedPreference<String> candidateHomeLng = sFile.stringValue("candidateHomeLng", "0.0");
    public static final File.SharedPreference<Long> candidatePrefJobRoleIdOne = sFile.longValue("candidatePrefJobRoleIdOne", 0L);
    public static final File.SharedPreference<Long> candidatePrefJobRoleIdTwo = sFile.longValue("candidatePrefJobRoleIdTwo", 0L);
    public static final File.SharedPreference<Long> candidatePrefJobRoleIdThree = sFile.longValue("candidatePrefJobRoleIdThree", 0L);

    public static final File.SharedPreference<Integer> candidateJobPrefStatus = sFile.intValue("candidateJobPrefStatus", 0);
    public static final File.SharedPreference<Integer> candidateHomeLocalityStatus = sFile.intValue("candidateHomeLocalityStatus", 0);

    public static final File.SharedPreference<String> jobPrefString = sFile.stringValue("jobPrefString", "");

    // apply job flags for not logged in
    public static final File.SharedPreference<Integer> jobToApplyStatus = sFile.intValue("jobToApplyFlag", 0);
    public static final File.SharedPreference<Long> getJobToApplyJobId = sFile.longValue("getJobToApplyJobId", 0L);

    public static void clearPrefValues() {
        Prefs.firstName.remove();
        Prefs.lastName.remove();
        Prefs.leadId.remove();
        Prefs.candidateMobile.remove();
        Prefs.candidateId.remove();
        Prefs.candidateMinProfile.remove();
        Prefs.candidateGender.remove();
        Prefs.isAssessed.remove();
        Prefs.sessionId.remove();
        Prefs.sessionExpiry.remove();
        Prefs.storedOtp.remove();
        Prefs.jobPostId.remove();
        Prefs.candidateHomeLocalityName.remove();
        Prefs.candidateHomeLat.remove();
        Prefs.candidateHomeLng.remove();
        Prefs.candidatePrefJobRoleIdOne.remove();
        Prefs.candidatePrefJobRoleIdTwo.remove();
        Prefs.candidatePrefJobRoleIdThree.remove();
        Prefs.candidateJobPrefStatus.remove();
        Prefs.candidateHomeLocalityStatus.remove();
        Prefs.jobPrefString.remove();
    }

    /* TODO maintain session and authToken across server and app */
    public static void onLogin(long leadId, long candidateId, String sessionId,
                                long sessionExpiry) {
        Prefs.leadId.put(leadId);
        Prefs.candidateId.put(candidateId);
        Prefs.sessionId.put(sessionId);
        Prefs.sessionExpiry.put(sessionExpiry);
    }

    public static class File {
        private static Context sContext;
        public static void init(Context context) {
            sContext = context;
        }
        private final String mFileName;
        public File(String name) {
            mFileName = name;
        }
        public SharedPreferences open() {
            return sContext.getSharedPreferences(mFileName, Context.MODE_PRIVATE);
        }

        public static boolean commit(SharedPreferences.Editor editor) {
            editor.apply();
            return true;
        }

        public SharedPreference<Long> longValue(final String key, final Long defaultValue) {
            return new SharedPreference<Long>(this, key) {
                @Override
                protected Long read(SharedPreferences sp) {
                    if (sp.contains(key)) {
                        return sp.getLong(key, 0L);
                    }
                    return defaultValue;
                }

                @Override
                protected void write(SharedPreferences.Editor editor, Long value) {
                    if (value == null) {
                        throw new IllegalArgumentException("null for Long");
                    }
                    editor.putLong(key, value);
                }
            };
        }

        public SharedPreference<String> stringValue(final String key, final String defaultValue) {
            return new SharedPreference<String>(this, key) {
                @Override
                protected String read(SharedPreferences sp) {
                    if (sp.contains(key)) {
                        return sp.getString(key, null);
                    }
                    return defaultValue;
                }

                @Override
                protected void write(SharedPreferences.Editor editor, String value) {
                    if (value == null) {
                        throw new IllegalArgumentException("null for String");
                    }
                    editor.putString(key, value);
                }
            };
        }

        public SharedPreference<Integer> intValue(final String key, final Integer defaultValue) {
            return new SharedPreference<Integer>(this, key) {
                @Override
                protected Integer read(SharedPreferences sp) {
                    if (sp.contains(key)) {
                        return sp.getInt(key, 0);
                    }
                    return defaultValue;
                }

                @Override
                protected void write(SharedPreferences.Editor editor, Integer value) {
                    if (value == null) {
                        throw new IllegalArgumentException("null for Integer");
                    }
                    editor.putInt(key, value);
                }
            };
        }

        public static abstract class SharedPreference<T> {
            File mFile;
            final String mKey;

            protected SharedPreference(File file, String key) {
                mFile = file;
                mKey = key;
            }

            public final T get() {
                return read(mFile.open());
            }

            public final String getKey() {
                return mKey;
            }

            public final boolean exists() {
                return mFile.open().contains(mKey);
            }

            public final void put(T value) {
                SharedPreferences sp = mFile.open();
                SharedPreferences.Editor editor = sp.edit();
                write(editor, value);
                commit(editor);
            }

            public final void remove() {
                commit(mFile.open().edit().remove(mKey));
            }

            protected abstract T read(SharedPreferences sp);

            protected abstract void write(SharedPreferences.Editor editor, T value);
        }
    }
}
