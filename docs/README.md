# GardenFlow Privacy Policy Site

This folder is designed to be published as a static GitHub Pages site.

## Files

- `index.html` — public Privacy Policy page.
- `styles.css` — plain responsive CSS.
- `PRIVACY_DATA_FLOW.md` — source-code privacy audit notes used to write the policy.

## Required Manual Values

Before using this page in Google Play Console, replace:

- `Tony Zhao`
- `tonyzhao32965@gmail.com`

Then publish the folder with GitHub Pages and configure the Android build with:

```properties
PRIVACY_POLICY_URL=https://YOUR_GITHUB_USERNAME.github.io/YOUR_REPOSITORY/
```

The app Settings screen opens `BuildConfig.PRIVACY_POLICY_URL` when configured.
