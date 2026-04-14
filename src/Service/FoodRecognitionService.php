<?php

namespace App\Service;

class FoodRecognitionService
{
    private string $calorieNinjasKey;
    private string $huggingfaceKey;

    public function __construct(
        string $calorieNinjasKey,
        string $huggingfaceKey
    ) {
        $this->calorieNinjasKey = $calorieNinjasKey;
        $this->huggingfaceKey = $huggingfaceKey;
    }

    public function analyzeImage(string $imagePath): array
    {
        if ($this->huggingfaceKey === '') {
            throw new \RuntimeException('Hugging Face API key not configured.');
        }
        if ($this->calorieNinjasKey === '') {
            throw new \RuntimeException('CalorieNinjas API key not configured.');
        }

        $imageData = base64_encode((string) file_get_contents($imagePath));
        $hfResponse = $this->requestJson(
            'https://router.huggingface.co/hf-inference/models/nateraw/food',
            [
                'Authorization: Bearer '.$this->huggingfaceKey,
                'Content-Type: application/json',
            ],
            json_encode([
                'inputs' => $imageData,
            ])
        );

        if (!is_array($hfResponse) || count($hfResponse) === 0) {
            throw new \RuntimeException('No food detected.');
        }
        $top = $hfResponse[0];
        $foodName = (string) ($top['label'] ?? '');
        $confidence = (float) ($top['score'] ?? 0);
        if ($foodName === '') {
            throw new \RuntimeException('No food name detected.');
        }

        $calorieResponse = $this->requestJson(
            'https://api.calorieninjas.com/v1/nutrition?query='.rawurlencode($foodName),
            [
                'X-Api-Key: '.$this->calorieNinjasKey,
            ]
        );

        $item = $calorieResponse['items'][0] ?? null;
        if (!$item) {
            throw new \RuntimeException('No nutrition data found.');
        }

        return [
            'food' => $foodName,
            'confidence' => round($confidence * 100, 1),
            'calories' => (float) ($item['calories'] ?? 0),
            'protein' => (float) ($item['protein_g'] ?? 0),
            'carbs' => (float) ($item['carbohydrates_total_g'] ?? 0),
            'fat' => (float) ($item['fat_total_g'] ?? 0),
        ];
    }

    private function requestJson(string $url, array $headers, ?string $body = null): array
    {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        if ($body !== null) {
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
        }
        $response = curl_exec($ch);
        $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        if ($response === false) {
            $err = curl_error($ch);
            curl_close($ch);
            throw new \RuntimeException('HTTP request failed: '.$err);
        }
        curl_close($ch);
        $data = json_decode($response, true);
        if (!is_array($data)) {
            throw new \RuntimeException('Invalid JSON response (HTTP '.$status.').');
        }
        if ($status >= 400) {
            $msg = $data['status']['description'] ?? $data['error']['message'] ?? $data['error'] ?? 'API error';
            throw new \RuntimeException($msg.' (HTTP '.$status.')');
        }
        return $data;
    }
}
