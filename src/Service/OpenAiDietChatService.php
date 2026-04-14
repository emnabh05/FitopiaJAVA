<?php

namespace App\Service;

class OpenAiDietChatService
{
    public function reply(string $message, array $context = []): string
    {
        $message = trim($message);
        if ($message === '') {
            throw new \RuntimeException('Message is empty.');
        }

        $firstName = trim((string) ($context['first_name'] ?? ''));
        $systemPrompt = 'You are a helpful nutrition assistant. Give concise, practical advice. '
            .'Do not diagnose diseases. If the request is medical, recommend consulting a professional.';

        if ($firstName !== '') {
            $systemPrompt .= ' User first name: '.$firstName.'.';
        }

        $messages = [
            ['role' => 'system', 'content' => $systemPrompt],
            ['role' => 'user', 'content' => $message],
        ];

        $groqKey = trim((string) ($_ENV['GROQ_API_KEY'] ?? $_SERVER['GROQ_API_KEY'] ?? ''));
        if ($groqKey !== '') {
            $data = $this->requestJson(
                'https://api.groq.com/openai/v1/chat/completions',
                [
                    'Authorization: Bearer '.$groqKey,
                    'Content-Type: application/json',
                ],
                [
                    'model' => 'llama-3.1-8b-instant',
                    'messages' => $messages,
                    'temperature' => 0.5,
                    'max_tokens' => 400,
                ]
            );

            $content = (string) ($data['choices'][0]['message']['content'] ?? '');
            if ($content === '') {
                throw new \RuntimeException('AI service returned an empty response.');
            }

            return trim($content);
        }

        $openAiKey = trim((string) ($_ENV['OPENAI_API_KEY'] ?? $_SERVER['OPENAI_API_KEY'] ?? ''));
        if ($openAiKey !== '') {
            $data = $this->requestJson(
                'https://api.openai.com/v1/chat/completions',
                [
                    'Authorization: Bearer '.$openAiKey,
                    'Content-Type: application/json',
                ],
                [
                    'model' => 'gpt-4o-mini',
                    'messages' => $messages,
                    'temperature' => 0.5,
                    'max_tokens' => 400,
                ]
            );

            $content = (string) ($data['choices'][0]['message']['content'] ?? '');
            if ($content === '') {
                throw new \RuntimeException('AI service returned an empty response.');
            }

            return trim($content);
        }

        throw new \RuntimeException('Missing API key. Set GROQ_API_KEY or OPENAI_API_KEY in .env.local.');
    }

    private function requestJson(string $url, array $headers, array $payload): array
    {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload, JSON_UNESCAPED_UNICODE));

        $response = curl_exec($ch);
        $status = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);

        if ($response === false) {
            $error = curl_error($ch);
            curl_close($ch);
            throw new \RuntimeException('AI request failed: '.$error);
        }

        curl_close($ch);

        $data = json_decode($response, true);
        if (!is_array($data)) {
            throw new \RuntimeException('Invalid AI response (HTTP '.$status.').');
        }

        if ($status >= 400) {
            $msg = $data['error']['message'] ?? 'AI service error';
            throw new \RuntimeException($msg.' (HTTP '.$status.')');
        }

        return $data;
    }
}