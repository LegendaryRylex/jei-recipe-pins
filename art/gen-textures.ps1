[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$out = Join-Path $PSScriptRoot '..\src\main\resources\assets\jeirecipepins\textures\gui'
New-Item -ItemType Directory -Force $out | Out-Null

magick -size 9x9 xc:none -fill '#ffffff' -draw 'rectangle 3,0 5,0' -draw 'rectangle 2,1 6,2' -draw 'rectangle 3,3 5,3' -draw 'rectangle 1,4 7,4' -draw 'rectangle 4,5 4,7' -fill '#b0b0b0' -draw 'point 2,1' -draw 'point 2,2' -draw 'point 3,3' -draw 'point 1,4' ("PNG32:" + (Join-Path $out 'pin.png'))
magick -size 9x9 xc:none -fill '#ffd700' -draw 'rectangle 3,0 5,0' -draw 'rectangle 2,1 6,2' -draw 'rectangle 3,3 5,3' -draw 'rectangle 1,4 7,4' -draw 'rectangle 4,5 4,7' -fill '#c89a00' -draw 'point 2,1' -draw 'point 2,2' -draw 'point 3,3' -draw 'point 1,4' ("PNG32:" + (Join-Path $out 'pin_active.png'))
