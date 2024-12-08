# Reservation System Coding Challenge

## Introduction

Thank you for your interest in joining Cove! We appreciate the time you're taking to complete this assessment.

The estimated time for completing this challenge is approximately ~1-2 hours. However, you're welcome to spend more or less time as you see fit. We understand that everyone works at different paces, and we're more interested in your approach and thought process than the exact time spent.

It's okay if you don't complete every aspect of the challenge or if some tests don't pass. We still want to see your submission!

## How To Get Started

### Programming Language

While we provide a project shell in TypeScript/JavaScript, we understand that you may be more comfortable with another programming language. You are welcome to use any language of your choice for this challenge. However, please note:

- If you choose to use a language other than TypeScript/JavaScript, you will need to set up a similar project structure yourself.
- Ensure that your chosen language can fulfill all the requirements of the challenge.
- Include clear instructions for setting up and running your project in your submission.

👉 Regardless of the language you choose, we will evaluate your solution based on its functionality, efficiency, and code quality.

### If you're using this TS bootstrap template

```bash
npm install
npm run test  // fails initially
npm run lint  // fails initially
```

## Problem Statement

Simple API Rate Limiter

Imagine we are offering a public API to customers and want to rate-limit each customer to a certain
number of requests within a defined time frame, e.g. 500 request/min. Customers are identified by a
unique clientId, e.g. the API-key.

For simplicity and easier testability in this exercise we simulate the passing of time through the
`timestamp` parameter passed into the rate-limiter call.

Requirements:
1. Implement a sliding window rate limiter that tracks requests within a specified time window
2. Support rate limit rule for a time window
   - e.g. max 5 requests per 10 seconds OR max 100 requests per hour
3. Track limits by client identifier (e.g. API key or IP address)

Behavior:
- The RateLimiter class maintains an internal state of all requests seen via `isRequestAllowed(request)`
- Each call to `isRequestAllowed(request)` both checks if the request is allowed, records it and returns
  `true` . If the request is denied it is **not** recorded and `false` is returned instead.
- Requests are assumed to come in chronological order by timestamp
- The implementation should cleanup old request data that's no longer needed

Example Rule: 5 requests per 10 seconds
```json
{
  windowSizeMs: 10000,  // 10 seconds
  maxRequests: 5
}
```

Please note that we only implemented a few test cases for demonstration purposes. Implement missing test cases as you see fit.


## Submission Guidelines

When you are done:
- run tests (see How To Get Started)
- run `npm run lint:fix`, it should not have any errors (there is a warning about the Typescript version, which is fine...)
- remove the node_module: `rm -rf ./node_modules`
- compress the repo into a ZIP file and
- upload the zip to Dropbox/Drive/... (in the past we had issues  sharing the zip directly via email due to our malware scanning, it should technically work now, but a file-share is playing it safe)
- email us your submission back!

## Final Thoughts

Remember, we're interested in your problem-solving approach and coding style. Don't stress if you can't complete everything – focus on demonstrating your strengths and thought process. Good luck, and we look forward to reviewing your submission!
